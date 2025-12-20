package net.minestom.server.instance.chunksystem;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.instance.InstanceChunkLoadEvent;
import net.minestom.server.event.instance.InstanceChunkUnloadEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.ChunkLoader;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.timer.TaskSchedule;
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
import java.util.concurrent.atomic.AtomicInteger;

import static net.minestom.server.coordinate.CoordConversion.chunkIndex;
import static net.minestom.server.instance.chunksystem.TaskSchedulerThread.Task;
import static net.minestom.server.instance.chunksystem.TaskSchedulerThread.link;

@SuppressWarnings("DuplicatedCode")
class SingleThreadedManager {
    static InternalCallbacks callbacks = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(SingleThreadedManager.class);

    final ChunkClaimTree tree = new ChunkClaimTree();
    final UpdateQueue updateQueue = new UpdateQueue(this);
    final UpdateHandler updateHandler = new UpdateHandler(this);

    private final TaskSchedulerThread taskSchedulerThread;
    /**
     * Identity strategy, so we can identify the correct claims and remove them.
     */
    final Object2ObjectMap<ChunkClaim, ClaimData> claimMap = new Object2ObjectOpenHashMap<>();
    private final Long2ObjectMap<Collection<ClaimData>> claimsByChunk = new Long2ObjectOpenHashMap<>();

    /**
     * Utility to track running tasks.
     * TODO Could help later with shutdown to ensure all tasks have finished before terminating the manager.
     */
    private final TaskTracking taskTracking = new TaskTracking();
    final ChunkWorker chunkWorker;
    final ChunkAccess chunkAccess;
    private final Instance instance;
    /**
     * Loaded chunks that are visible from the outside.
     * This is thread-safe for read-only usages.
     * <p>
     * This is updated on the chunk's own tick thread for consistency with
     * chunk loading/unloading events
     */
    private final Long2ObjectSyncMap<Chunk> loadedChunks = Long2ObjectSyncMap.hashmap();
    /**
     * Loaded chunks that are visible from the outside.
     * This is thread-safe for read-only usages.
     * <p>
     * This is updated on the manager thread. This here so chunks can be found immediately
     * after being loaded, and not only once the chunk first ticks.
     */
    final Long2ObjectSyncMap<Chunk> loadedChunksManaged = Long2ObjectSyncMap.hashmap();

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
    @NotNull ChunkLoader chunkLoader;
    /**
     * used to supply a new chunk object at a position when requested
     */
    @NotNull ChunkSupplier chunkSupplier;
    boolean autosaveEnabled;

    public SingleThreadedManager(@NotNull TaskSchedulerThread taskSchedulerThread, @NotNull ChunkAccess chunkAccess, @NotNull ChunkLoader chunkLoader, @NotNull ChunkSupplier chunkSupplier) {
        this.taskSchedulerThread = taskSchedulerThread;
        this.chunkAccess = chunkAccess;
        this.chunkLoader = chunkLoader;
        this.chunkSupplier = chunkSupplier;
        this.chunkWorker = new ChunkWorker(taskSchedulerThread, chunkAccess);
        this.instance = taskSchedulerThread.getInstance();
    }

    @NotNull
    @UnmodifiableView
    Collection<Chunk> loadedChunks() {
        return Collections.unmodifiableCollection(this.loadedChunks.values());
    }

    @NotNull
    @UnmodifiableView
    Collection<Chunk> loadedChunksManaged() {
        return Collections.unmodifiableCollection(this.loadedChunksManaged.values());
    }

    @Nullable Chunk loadedChunk(int x, int z) {
        return this.loadedChunks.get(chunkIndex(x, z));
    }

    @Nullable Chunk loadedChunkManaged(int x, int z) {
        return this.loadedChunksManaged.get(chunkIndex(x, z));
    }

    /**
     * This method is responsible for selecting and submitting the chunks to load.
     */
    IterationResult workIteration() {
        var count = 0;
        while (true) {
            if (count++ == 5) {
                // Exit this loop and restart it, this makes sure all tasks are handled
                // If there are no tasks, this will immediately restart workIteration
                return IterationResult.RUN_AGAIN;
            }

            var update = this.updateQueue.dequeue();
            if (update == null) break;
            var claimData = this.claimMap.get(update.origin());
            var disablePropagation = this.updateQueue.lastRemovedDisablePropagation();

            var result = this.updateHandler.workUpdate(update, disablePropagation);
            if (result instanceof UpdateResult.WaitingForWorker) {
                // The worker is busy. Exit loop here
                // We need to submit the update again, it hasn't been handled yet
                this.updateQueue.enqueue(update, null);
                if (this.updateQueue.resetUpdated()) {
                    return IterationResult.RUN_AGAIN;
                }
                return IterationResult.WAIT_FOR_SIGNAL_OR_WORKER;
            } else if (result instanceof UpdateResult.WaitingForFuture(var future, var d)) {
                future.whenComplete((o, throwable) -> this.taskSchedulerThread.addTask(new Task.EnqueueUpdate(update, claimData, d)));
                continue;
            }

            if (claimData != null) {
                claimData.updateDequeued(update);
            }
        }
        if (this.updateQueue.resetUpdated()) {
            return IterationResult.RUN_AGAIN;
        }
        return IterationResult.WAIT_FOR_SIGNAL;
    }

    void addClaim(ChunkAndClaim chunkAndClaim) {
        var claim = chunkAndClaim.claim();
        var x = claim.chunkX();
        var z = claim.chunkZ();
        this.tree.insert(x, z, claim.radius(), claim.priority(), claim.shape());

        var claimData = new ClaimData(claim, chunkAndClaim.chunkFuture());
        var chunkIndex = chunkIndex(x, z);
        this.claimMap.put(claim, claimData);
        this.claimsByChunk.computeIfAbsent(chunkIndex, c -> new HashSet<>(4)).add(claimData);

        // If the chunk is already loaded, we can complete the future right here.
        // The new claim has been inserted into the map, the chunk may not be unloaded
        // as long as the claim exists, so there shouldn't be any issues with this.
        this.completeIfLoaded(x, z, chunkAndClaim);
        this.submitUpdate(x, z, UpdateType.ADD_CLAIM_EXPLICIT, claim, claimData);

        if (callbacks != null) {
            callbacks.onAddClaim(x, z, claim);
        }
    }

    void addCopiedClaim(ChunkAndClaim chunkAndClaim) {
        var claim = chunkAndClaim.claim();
        var x = claim.chunkX();
        var z = claim.chunkZ();
        this.tree.insert(x, z, claim.radius(), claim.priority(), claim.shape());

        var claimData = new ClaimData(claim, chunkAndClaim.chunkFuture());
        var chunkIndex = chunkIndex(x, z);
        this.claimMap.put(claim, claimData);
        this.claimsByChunk.computeIfAbsent(chunkIndex, c -> new HashSet<>(4)).add(claimData);
    }

    @NotNull Collection<ChunkAndClaim> singleClaimCopy(@NotNull SingleThreadedManager copyTarget, int priority) {
        var copiedChunks = this.updateHandler.singleClaimCopyTo(copyTarget.updateHandler, copyTarget.instance);
        var list = new ArrayList<ChunkAndClaim>();
        var dispatcher = MinecraftServer.process().dispatcher();
        for (var chunk : copiedChunks) {
            var chunkX = chunk.getChunkX();
            var chunkZ = chunk.getChunkZ();
            var index = CoordConversion.chunkIndex(chunkX, chunkZ);
            var claim = new ChunkClaimImpl(chunkX, chunkZ, 0, priority, ChunkClaim.Shape.SQUARE, null);
            var chunkAndClaim = new ChunkAndClaim(CompletableFuture.completedFuture(chunk), claim);
            copyTarget.addCopiedClaim(chunkAndClaim);
            list.add(chunkAndClaim);
            copyTarget.loadedChunks.put(index, chunk);
            copyTarget.loadedChunksManaged.put(index, chunk);

            dispatcher.createPartition(chunk);
        }
        return List.copyOf(list);
    }

    void removeClaim(ChunkClaim claim, CompletableFuture<Void> future) {
        var claimedChunk = this.claimMap.remove(claim);
        if (claimedChunk == null) {
            this.taskSchedulerThread.completeExceptionally(future, new IllegalStateException("The claim you attempted to remove is not valid"));
            return;
        }
        var x = claimedChunk.claim.chunkX();
        var z = claimedChunk.claim.chunkZ();
        var chunkIndex = chunkIndex(x, z);
        var claims = this.claimsByChunk.get(chunkIndex);
        claims.remove(claimedChunk);
        if (claims.isEmpty()) {
            this.claimsByChunk.remove(chunkIndex);
        }

        this.tree.delete(x, z, claim.radius(), claim.priority(), claim.shape());
        this.submitUpdate(x, z, UpdateType.REMOVE_CLAIM_EXPLICIT, claim, claimedChunk);
        // We can complete the future right here, the claim was removed.
        // Removing a claim makes no guarantees about when the chunk is unloaded, so this is the easiest
        // and most obvious place to complete the future
        this.taskSchedulerThread.complete(future, null);
        this.taskSchedulerThread.completeExceptionally(claimedChunk.mainChunkFuture, new CancellationException("Claim was removed"));

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
        var claims = this.claimsByChunk.get(chunkIndex);
        if (claims != null) {
            for (var claim : claims) {
                this.taskSchedulerThread.complete(claim.mainChunkFuture, chunk);
            }
        }
        // add this scheduler before adding the dispatcher partition to make sure it is executed first
        this.taskTracking.runningTickScheduledCount.incrementAndGet();
        Runnable task = () -> {
            assert !this.loadedChunks.containsKey(chunkIndex);
            this.loadedChunks.put(chunkIndex, chunk);
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
            this.scheduleOnChunk(chunk, task);
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
        var chunk = this.updateHandler.getLoaded(x, z);
        if(chunk == null) return;
        this.taskSchedulerThread.complete(chunkAndClaim.chunkFuture(), chunk);
    }

    private void submitUpdate(int x, int z, UpdateType updateType, ChunkClaim claim, @NotNull ClaimData claimData) {
        this.updateQueue.enqueue(new PrioritizedUpdate(updateType, claim.priority(), x, z, claim), claimData);
        if (ServerFlag.INSIDE_TEST) {
            if (this.updateQueue.size() > 50000) {
                // just so we can set a breakpoint here.
                // conditional breakpoints slow everything down x500
                // TODO remove

                //noinspection UnnecessaryReturnStatement
                return;
            }
        }
    }

    private void scheduleOnChunk(Chunk chunk, Runnable task) {
        chunk.getScheduler().scheduleTask(task, TaskSchedule.tick(10), TaskSchedule.stop());
//        chunk.getScheduler().scheduleNextProcess(task); TODO use this again, line above is to provoke problems while debugging
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
        this.loadedChunksManaged.remove(chunkIndex, chunk);
        this.taskTracking.runningTickScheduledCount.addAndGet(2);
        Runnable runInstance = () -> {
            this.instance.getEntityTracker().chunkEntities(chunk.getChunkX(), chunk.getChunkZ(), EntityTracker.Target.ENTITIES).forEach(e -> {
                if (e instanceof Player p) {
                    LOGGER.warn("Disconnecting player because of unloaded chunk {}", p.getUsername());
                    p.kick("Your chunk was unloaded");
                }
                else e.remove();
            });
            this.taskTracking.runningTickScheduledCount.decrementAndGet();
        };
        Runnable runChunk = () -> {
            EventDispatcher.call(new InstanceChunkUnloadEvent(this.instance, chunk));
            this.chunkAccess.unload(chunk);
            MinecraftServer.process().dispatcher().deletePartition(chunk);
            assert this.loadedChunks.get(chunkIndex) == chunk;
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
            this.scheduleOnChunk(chunk, runChunk);
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

        record UpdatePropagatedForLoaded() implements UpdateResult {
        }

        record WaitingForWorker() implements UpdateResult {
        }

        record WaitingForFuture(CompletableFuture<?> future, boolean disablePropagation) implements UpdateResult {
        }
    }

    static void executeVirtual(Runnable runnable) {
        if (ServerFlag.ASYNC_CHUNK_SYSTEM) {
            Thread.startVirtualThread(runnable);
        } else {
            runnable.run();
        }
    }

    static final class ClaimData {
        final ChunkClaim claim;
        final CompletableFuture<Chunk> mainChunkFuture;
        /**
         * Simple counter, starts at 0, then starts counting up.
         * This counter will be back at 0 when all chunks have been loaded.
         * This counter can have multiple increments for a single chunk,
         * to ensure the correct order of callback invocations.
         */
        private final AtomicInteger counter = new AtomicInteger();

        public ClaimData(ChunkClaim claim, CompletableFuture<Chunk> mainChunkFuture) {
            this.claim = claim;
            this.mainChunkFuture = mainChunkFuture;
        }

        void startLoad() {
            counter.incrementAndGet();
        }

        void finishLoad() {
            var loads = counter.decrementAndGet();

            if (loads == 0) {
                var callbacks = claim.callbacks();
                if (callbacks != null) {
                    // Start a virtual thread. All load callbacks have finished.
                    // We don't want to give the callback the option of blocking the chunk manager,
                    // so we call it on a virtual thread.
                    executeVirtual(() -> {
                        try {
                            callbacks.allChunksLoaded(claim);
                        } catch (Throwable t) {
                            LOGGER.error("Exception in #allChunksLoaded callback", t);
                        }
                    });
                }
            }
        }

        public void updateEnqueued(PrioritizedUpdate update) {
            if (update.updateType().isLoad()) {
                startLoad();
            }
        }

        public void updateDequeued(PrioritizedUpdate update) {
            if (update.updateType().isLoad()) {
                finishLoad();
            }
        }
    }
}
