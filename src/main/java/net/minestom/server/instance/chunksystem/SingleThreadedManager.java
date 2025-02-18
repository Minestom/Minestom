package net.minestom.server.instance.chunksystem;

import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectSortedSet;
import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.instance.InstanceChunkLoadEvent;
import net.minestom.server.event.instance.InstanceChunkUnloadEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.network.packet.server.play.UnloadChunkPacket;
import net.minestom.server.utils.chunk.ChunkSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.vectrix.flare.fastutil.Long2ObjectSyncMap;

import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;

import static net.minestom.server.coordinate.CoordConversion.chunkIndex;
import static net.minestom.server.instance.chunksystem.TaskSchedulerThread.Task;
import static net.minestom.server.instance.chunksystem.TaskSchedulerThread.link;

class SingleThreadedManager {
    static InternalCallbacks callbacks = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(SingleThreadedManager.class);

    final ChunkClaimTree tree = new ChunkClaimTree();
    final UpdateQueue updateQueue = new UpdateQueue(this);
    final UpdateHandler updateHandler = new UpdateHandler(this);

    private final TaskSchedulerThread taskSchedulerThread;
//    private final Long2DoubleMap chunksHighestUpdatePriority = new Long2DoubleOpenHashMap();
//    private final Long2DoubleMap chunksHighestRadius = new Long2DoubleOpenHashMap();
//    private final Long2ObjectMap<LoadTask> loadingChunks = new Long2ObjectOpenHashMap<>();
    /**
     * This HashMap is used to quickly access loaded chunks in case of added claims after they are scheduled for unload.
     * Otherwise, we'd have to wait for the unload/write to finish, and only then could we start the next load.
     * With this, we cache the chunk, and if it is loaded again, we can just copy it and use the new copy.
     * This way we also ensure the chunks are not changed while saving, which may cause issues with the ChunkLoader/-Saver.
     */
//    private final Long2ObjectMap<UnloadTask> unloadingChunks = new Long2ObjectOpenHashMap<>();
//    private final Long2ObjectMap<CompletableFuture<Void>> unloadFutures = new Long2ObjectOpenHashMap<>();
    /**
     * HashMap for saving chunks. We don't want to save the same chunk concurrently, and we don't want unnecessary saves
     */
//    private final Long2ObjectMap<SaveTask> savingChunks = new Long2ObjectOpenHashMap<>();
    /**
     * HashMap to contain all chunks that have their save request delayed. A chunk may be requested for saving
     * while it is being saved already. This save will work on old data though, we want the newest data, so we have
     * to reschedule the save after the first save finishes.
     */
//    private final Long2ObjectMap<SaveTask> savingChunksDelayed = new Long2ObjectOpenHashMap<>();
    /**
     * Identity strategy, so we can identify the correct claims and remove them.
     */
    private final Object2ObjectMap<ChunkClaim, ClaimedChunk> claimMap = new Object2ObjectOpenHashMap<>();
    private final ReentrantLock claimsByChunkLock = new ReentrantLock();
    private final Long2ObjectMap<Collection<ClaimedChunk>> claimsByChunk = new Long2ObjectOpenHashMap<>();

    /**
     * Utility to track running tasks.
     * TODO Could help later with shutdown to ensure all tasks have finished before terminating the manager.
     */
    private final TaskTracking taskTracking = new TaskTracking();
    final ChunkWorker chunkWorker;
    private final ChunkAccess chunkAccess;
    private final Instance instance;
    /**
     * (chunk index -> chunk) map, contains all the chunks in the instance.
     */
//    private final Long2ObjectMap<Chunk> chunks = new Long2ObjectOpenHashMap<>();
    /**
     * Loaded chunks that are visible to the outside.
     * This is thread-safe for read-only usages.
     */
    private final Long2ObjectSyncMap<Chunk> loadedChunks = Long2ObjectSyncMap.hashmap();
    //    private boolean savingInstance = false;
//    private CompletableFuture<Void> savingInstanceDelayed = null;

    PriorityDrop priorityDrop = switch (ServerFlag.CHUNK_SYSTEM_PRIORITY_DROP) {
        case "simple" -> new PriorityDrop.Simple();
        case "hypotenuse" -> new PriorityDrop.Hypotenuse();
        case "square" -> new PriorityDrop.Square();
        case null, default -> new PriorityDrop.HypotenuseSquared();
    };
    /**
     * TODO in the InstanceContainer code this was volatile. Check why? Other fields like chunkLoader should also have been volatile?
     * the chunk generator used, can be null
     */
    @Nullable Generator generator;
    /**
     * the chunk loader, used when trying to load/save a chunk from another source
     */
    @NotNull IChunkLoader chunkLoader;
    /**
     * used to supply a new chunk object at a position when requested
     */
    @NotNull ChunkSupplier chunkSupplier;
    boolean autosaveEnabled;

    public SingleThreadedManager(@NotNull TaskSchedulerThread taskSchedulerThread, @NotNull ChunkAccess chunkAccess, @NotNull IChunkLoader chunkLoader, @NotNull ChunkSupplier chunkSupplier) {
        this.taskSchedulerThread = taskSchedulerThread;
        this.chunkAccess = chunkAccess;
        this.chunkLoader = chunkLoader;
        this.chunkSupplier = chunkSupplier;
        this.chunkWorker = new ChunkWorker(taskSchedulerThread, chunkAccess);
        this.instance = taskSchedulerThread.getInstance();
//        this.chunksHighestUpdatePriority.defaultReturnValue(Double.NaN);
//        this.chunksHighestRadius.defaultReturnValue(Double.NaN);
    }

    @NotNull
    @UnmodifiableView
    Collection<Chunk> loadedChunks() {
        return Collections.unmodifiableCollection(this.loadedChunks.values());
    }

    @NotNull Chunk loadedChunk(int x, int z) {
        return this.loadedChunks.get(chunkIndex(x, z));
    }

    /**
     * This method is responsible for selecting and submitting the chunks to load.
     */
    IterationResult workIteration() {
        // This loop should not run very long. It will first do any maintenance work (removing stale updates)
        // then it will submit updates as long as the workers can handle these updates, then it will exit,
        // because otherwise the workers would be overloaded
        var count = 0;
        while (true) {
            if (count++ == 5) {
                // Exit this loop and restart it, this makes sure all tasks are handled
                // If there are no tasks, this will immediately restart workIteration
                return IterationResult.RUN_AGAIN;
            }

            var update = this.updateQueue.dequeue();
            if (update == null) break;
            var disablePropagation = this.updateQueue.lastRemovedDisablePropagation();

            var result = this.updateHandler.workUpdate(update, disablePropagation);
            if (result instanceof UpdateResult.WaitingForWorker) {
                // The worker is busy. Exit loop here
                // We need to submit the update again, it hasn't been handled yet
                this.updateQueue.enqueue(update);
                if (this.updateQueue.resetUpdated()) {
                    return IterationResult.RUN_AGAIN;
                }
                return IterationResult.WAIT_FOR_SIGNAL_OR_WORKER;
            } else if (result instanceof UpdateResult.WaitingForFuture(var future, var d)) {
                future.whenComplete((o, throwable) -> this.taskSchedulerThread.addTask(new Task.EnqueueUpdate(update, d)));
            }
        }
        if (this.updateQueue.resetUpdated()) {
            return IterationResult.RUN_AGAIN;
        }
        return IterationResult.WAIT_FOR_SIGNAL;
    }

    void addClaim(int x, int z, ChunkAndClaim chunkAndClaim) {
        var claim = chunkAndClaim.chunkClaim();
        this.tree.insert(x, z, claim.radius(), claim.priority(), claim.shape());

        var claimedChunk = new ClaimedChunk(x, z, chunkAndClaim.chunkFuture());
        var chunkIndex = chunkIndex(x, z);
        this.claimMap.put(claim, claimedChunk);
        this.claimsByChunkLock.lock();
        try {
            this.claimsByChunk.computeIfAbsent(chunkIndex, c -> new HashSet<>(4)).add(claimedChunk);
        } finally {
            this.claimsByChunkLock.unlock();
        }

        // If the chunk is already loaded, we can complete the future right here.
        // The new claim has been inserted into the map, the chunk may not be unloaded
        // as long as the claim exists, so there shouldn't be any issues with this.
        this.completeIfLoaded(x, z, chunkAndClaim);
        this.submitUpdate(x, z, x, z, UpdateType.ADD_CLAIM_EXPLICIT, claim);

        if (callbacks != null) {
            callbacks.onAddClaim(x, z, claim);
        }
    }

    void removeClaim(ChunkClaim claim, CompletableFuture<Void> future) {
        var claimedChunk = this.claimMap.remove(claim);
        if (claimedChunk == null) {
            this.taskSchedulerThread.completeExceptionally(future, new IllegalStateException("The claim you attempted to remove is not valid"));
            return;
        }
        var x = claimedChunk.x();
        var z = claimedChunk.z();
        var chunkIndex = chunkIndex(x, z);
        this.claimsByChunkLock.lock();
        try {
            var claims = this.claimsByChunk.get(chunkIndex);
            claims.remove(claimedChunk);
            if (claims.isEmpty()) {
                this.claimsByChunk.remove(chunkIndex);
            }
        } finally {
            this.claimsByChunkLock.unlock();
        }

        this.tree.delete(x, z, claim.radius(), claim.priority(), claim.shape());
        this.submitUpdate(x, z, x, z, UpdateType.REMOVE_CLAIM_EXPLICIT, claim);
        // We can complete the future right here, the claim was removed.
        // Removing a claim makes no guarantees about when the chunk is unloaded, so this is the easiest
        // and most obvious place to complete the future
        this.taskSchedulerThread.complete(future, null);
        this.taskSchedulerThread.completeExceptionally(claimedChunk.future(), new CancellationException("Claim was removed"));

        if (callbacks != null) {
            callbacks.onRemoveClaim(x, z, claim);
        }
    }

    boolean hasClaim(ChunkClaim claim) {
        return this.claimMap.containsKey(claim);
    }

    void saveChunk(Chunk chunk, CompletableFuture<Void> future) {
        this.updateHandler.saveChunk(chunk, future);
    }

    void startSavingChunk(Chunk chunk, UpdateHandler.SaveState saveState) {
        this.runOnSaveExecutor(() -> {
            try {
                this.chunkLoader.saveChunk(chunk);
                this.taskSchedulerThread.complete(saveState.future, null);
            } catch (Throwable t) {
                this.taskSchedulerThread.completeExceptionally(saveState.future, t);
            } finally {
                this.taskSchedulerThread.addTask(new TaskSchedulerThread.Task.SaveChunkCompleted(chunk));
            }
        });
    }

    void saveChunkCompleted(Chunk chunk) {
        this.updateHandler.saveChunkCompleted(chunk);
    }

    void saveInstanceData(CompletableFuture<Void> future) {
        this.updateHandler.saveInstanceData(future);
    }

    void startSaveInstance(CompletableFuture<Void> future) {
        var chunkLoader = this.chunkLoader;
        this.runOnSaveExecutor(() -> {
            try {
                chunkLoader.saveInstance(this.instance);
                this.taskSchedulerThread.complete(future, null);
            } catch (Throwable t) {
                this.taskSchedulerThread.completeExceptionally(future, t);
            } finally {
                this.taskSchedulerThread.addTask(new TaskSchedulerThread.Task.SaveInstanceDataCompleted());
            }
        });
    }

    void saveInstanceCompleted() {
        this.updateHandler.saveInstanceCompleted();
    }

    void saveChunks(CompletableFuture<Void> future) {
        this.updateHandler.saveAllChunks(future);
    }

    void saveInstanceDataAndChunks(CompletableFuture<Void> future) {
        var chunks = new CompletableFuture<Void>();
        this.saveChunks(chunks);
        var data = new CompletableFuture<Void>();
        this.saveInstanceData(data);
        link(CompletableFuture.allOf(chunks, data), future);
    }

    void chunkGenerationFinished(Chunk chunk) {
        // The claim may have been removed by now. We will first have to check that
        var x = chunk.getChunkX();
        var z = chunk.getChunkZ();
        if (callbacks != null) {
            callbacks.onGenerationCompleted(x, z);
        }
        if (!this.updateHandler.tryChangeToLoaded(chunk)) {
            return;
        }

        var chunkIndex = chunkIndex(x, z);

        // add this scheduler before adding the dispatcher partition to make sure it is executed first
        this.taskTracking.runningTickScheduledCount.incrementAndGet();
        Runnable task = () -> {
            var old = this.loadedChunks.put(chunkIndex, chunk);
            List<ClaimedChunk> claimsCopy;
            this.claimsByChunkLock.lock();
            try {
                var claims = this.claimsByChunk.get(chunkIndex);
                // the claims could have been removed by now, the collection could be null
                claimsCopy = claims == null ? List.of() : List.copyOf(claims);
            } finally {
                this.claimsByChunkLock.unlock();
            }
            for (var claim : claimsCopy) {
                claim.future().complete(chunk);
            }
            if (old != null) {
                LOGGER.error("Existing chunk loaded at ({}, {}): {}", x, z, old);
            }
            var event = new InstanceChunkLoadEvent(this.instance, chunk);
            EventDispatcher.call(event);
            this.taskTracking.runningTickScheduledCount.decrementAndGet();
        };
        // We must make sure to create the partition before executing the "task" runnable.
        // Otherwise, the chunk partition may not exist when the chunk load has finished,
        // and further entity partition calls will not work
        MinecraftServer.process().dispatcher().createPartition(chunk);

        if (!ServerFlag.ASYNC_CHUNK_SYSTEM) {
            task.run();
        } else {
            chunk.getScheduler().scheduleNextProcess(task);
        }
    }

    void finishUnloadAfterPartition(UpdateHandler.State.Unloading unloading) {
        var chunk = unloading.chunk;
        if (this.autosaveEnabled) {
            // This future will be completed once the chunk has been saved
            var saveFuture = new CompletableFuture<Void>();

            this.saveChunk(chunk, saveFuture);
            saveFuture.whenComplete((unused, throwable) -> {
                if (throwable != null) {
                    LOGGER.error("Exception when saving chunk", throwable);
                    return;
                }
                this.taskSchedulerThread.addTask(new Task.FinishUnloadAfterSaveAndPartition(unloading));
            });
        } else {
            this.finishUnloadChunkAfterSaveAndPartition(unloading);
        }
        if (callbacks != null) {
            var x = chunk.getChunkX();
            var z = chunk.getChunkZ();
            callbacks.onUnloadCompleted(x, z);
        }
    }

    void finishUnloadChunkAfterSaveAndPartition(@NotNull UpdateHandler.State.Unloading unloading) {
        this.updateHandler.finishUnloadAfterSaveAndPartition(unloading);
        this.taskSchedulerThread.complete(unloading.unloadFuture, null);
    }

    void completeIfLoaded(int x, int z, ChunkAndClaim chunkAndClaim) {
        var index = chunkIndex(x, z);
        var chunk = this.loadedChunks.get(index);
        if (chunk == null) return;
        this.taskSchedulerThread.complete(chunkAndClaim.chunkFuture(), chunk);
    }

    private void submitUpdate(int claimX, int claimZ, int x, int z, UpdateType updateType, ChunkClaim claim) {
        this.updateQueue.enqueue(new PrioritizedUpdate(updateType, claim.priority(), x, z, claimX, claimZ, claim));
        if (ServerFlag.INSIDE_TEST) {
            if (this.updateQueue.size() > 50000) {
                // just so we can set a breakpoint here.
                // conditional breakpoints slow everything down x500

                //noinspection UnnecessaryReturnStatement
                return;
            }
        }
    }

    void startWorkerGenerateChunk(int x, int z) {
        @NotNull var loader = this.chunkLoader;
        @NotNull var supplier = this.chunkSupplier;
        @Nullable var generator = this.generator;
        this.runOnWorkerWithReservation(() -> {
            if (callbacks != null) {
                callbacks.onGenerationStarted(x, z);
            }
            try {
                this.chunkWorker.workerGenerateChunk(x, z, loader, supplier, generator);
            } catch (Throwable throwable) {
                LOGGER.error("Exception during chunk loading/generation", throwable);
            }
        });
    }

    void startWorkerCopyFromMemory(Chunk chunk, int x, int z) {
        this.runOnWorkerWithReservation(() -> {
            if (callbacks != null) {
                callbacks.onGenerationStarted(x, z);
            }
            try {
                this.chunkWorker.workerCopyFromMemory(chunk, x, z);
            } catch (Throwable throwable) {
                LOGGER.error("Exception when copying chunk from old (in memory)", throwable);
            }
        });
    }

    void startUnloadChunk(UpdateHandler.State.Unloading unloading) {
        var chunk = unloading.chunk;
        var x = chunk.getChunkX();
        var z = chunk.getChunkZ();
        var chunkIndex = chunkIndex(x, z);
        if (callbacks != null) {
            callbacks.onUnloadStarted(x, z);
        }
        this.taskTracking.runningTickScheduledCount.addAndGet(2);
        Runnable runInstance = () -> {
            this.instance.getEntityTracker().chunkEntities(chunk.getChunkX(), chunk.getChunkZ(), EntityTracker.Target.ENTITIES).forEach(e -> {
                if (e instanceof Player p) p.kick("Your chunk was unloaded");
                else e.remove();
            });
            this.taskTracking.runningTickScheduledCount.decrementAndGet();
        };
        Runnable runChunk = () -> {
            chunk.sendPacketToViewers(new UnloadChunkPacket(x, z));
            EventDispatcher.call(new InstanceChunkUnloadEvent(this.instance, chunk));
            this.chunkAccess.unload(chunk);
            MinecraftServer.process().dispatcher().deletePartition(chunk);
            if (!this.loadedChunks.remove(chunkIndex, chunk)) {
                LOGGER.error("Failed to remove loaded chunk at ({}, {}): {}", x, z, chunk);
            }
            this.taskSchedulerThread.complete(unloading.partitionDeleted, null);
            this.taskSchedulerThread.addTask(new Task.FinishUnloadAfterPartition(unloading));
            this.taskTracking.runningTickScheduledCount.decrementAndGet();
        };
        if (!ServerFlag.ASYNC_CHUNK_SYSTEM) {
            runInstance.run();
            runChunk.run();
        } else {
            this.instance.scheduler().scheduleNextProcess(runInstance);
            chunk.getScheduler().scheduleNextProcess(runChunk);
        }
    }

    void runOnWorkerWithReservation(Runnable runnable) {
        this.taskTracking.runningWorkerTaskCount.incrementAndGet();
        ChunkWorker.submitReserved(() -> {
            try {
                runnable.run();
            } finally {
                this.taskTracking.runningWorkerTaskCount.decrementAndGet();
            }
        });
    }

    void runOnSaveExecutor(Runnable runnable) {
        this.taskTracking.runningSaveTaskCount.incrementAndGet();
        ChunkWorker.runOnSaveExecutor(() -> {
            try {
                runnable.run();
            } finally {
                this.taskTracking.runningSaveTaskCount.decrementAndGet();
            }
        });
    }

    enum IterationResult {
        RUN_AGAIN,
        WAIT_FOR_SIGNAL,
        WAIT_FOR_SIGNAL_OR_WORKER
    }

    // TODO I thought I'm gonna need to pass data to these, maybe I don't need to though.
    //  Check later and maybe convert to enum.
    interface UpdateResult {
        UpdateResult INVALID_UPDATE = new InvalidUpdate();
        UpdateResult LOAD_SCHEDULED = new LoadScheduled();
        UpdateResult LOAD_SCHEDULED_EXISTING = new WaitForScheduledLoad();
        UpdateResult UNLOAD_SCHEDULED = new UnloadScheduled();
        UpdateResult ALREADY_UP_TO_DATE = new AlreadyUpToDate();
        UpdateResult UPDATE_PROPAGATED_FOR_LOADED = new UpdatePropagatedForLoaded();
        UpdateResult WAITING_FOR_WORKER = new WaitingForWorker();
        UpdateResult LOAD_COPY_SCHEDULED = new LoadCopyScheduled();

        record LoadCopyScheduled() implements UpdateResult {
        }

        record InvalidUpdate() implements UpdateResult {
        }

        record LoadScheduled() implements UpdateResult {
        }

        record UnloadScheduled() implements UpdateResult {
        }

        record WaitForScheduledLoad() implements UpdateResult {
        }

        record AlreadyUpToDate() implements UpdateResult {
        }

        record UpdatePropagatedForLoaded() implements UpdateResult {
        }

        record WaitingForWorker() implements UpdateResult {
        }

        record WaitingForFuture(CompletableFuture<?> future, boolean disablePropagation) implements UpdateResult {
        }
    }

    private record ClaimedChunk(int x, int z, CompletableFuture<Chunk> future) {
    }
}
