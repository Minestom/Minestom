package net.minestom.server.instance.chunksystem;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectHeapPriorityQueue;
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

import static net.minestom.server.instance.chunksystem.TaskSchedulerThread.*;

class SingleThreadedManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(SingleThreadedManager.class);
    private final ChunkClaimTree tree = new ChunkClaimTree();
    private final TaskSchedulerThread taskSchedulerThread;
    private final Long2DoubleMap chunksLastUpdatePriority = new Long2DoubleOpenHashMap();
    private final Long2ObjectMap<TaskSchedulerThread.LoadTask> loadingChunks = new Long2ObjectOpenHashMap<>();
    /**
     * This HashMap is used to quickly access loaded chunks in case of added claims after they are scheduled for unload.
     * Otherwise, we'd have to wait for the unload/write to finish, and only then could we start the next load.
     * With this, we cache the chunk, and if it is loaded again, we can just copy it and use the new copy.
     * This way we also ensure the chunks are not changed while saving, which may cause issues with the ChunkLoader/-Saver.
     */
    private final Long2ObjectMap<TaskSchedulerThread.UnloadTask> unloadingChunks = new Long2ObjectOpenHashMap<>();
    private final Long2ObjectMap<CompletableFuture<Void>> unloadFutures = new Long2ObjectOpenHashMap<>();
    /**
     * HashMap for saving chunks. We don't want to save the same chunk concurrently, and we don't want unnecessary saves
     */
    private final Long2ObjectMap<TaskSchedulerThread.SaveTask> savingChunks = new Long2ObjectOpenHashMap<>();
    /**
     * HashMap to contain all chunks that have their save request delayed. A chunk may be requested for saving
     * while it is being saved already. This save will work on old data though, we want the newest data, so we have
     * to reschedule the save after the first save finishes.
     */
    private final Long2ObjectMap<TaskSchedulerThread.SaveTask> savingChunksDelayed = new Long2ObjectOpenHashMap<>();
    /**
     * Identity strategy, so we can identify the correct claims and remove them.
     */
    private final Object2ObjectMap<ChunkClaim, TaskSchedulerThread.ClaimedChunk> claimMap = new Object2ObjectOpenHashMap<>();
    private final ReentrantLock claimsByChunkLock = new ReentrantLock();
    private final Long2ObjectMap<Collection<TaskSchedulerThread.ClaimedChunk>> claimsByChunk = new Long2ObjectOpenHashMap<>();
    private final ObjectHeapPriorityQueue<PrioritizedUpdate> updateQueue = new ObjectHeapPriorityQueue<>(PrioritizedUpdate.COMPARATOR);
    /**
     * Utility to track running tasks.
     * TODO Could help later with shutdown to ensure all tasks have finished before terminating the manager.
     */
    private final TaskTracking taskTracking = new TaskTracking();
    private final ChunkWorker chunkWorker;
    private final ChunkAccess chunkAccess;
    private final Instance instance;
    /**
     * (chunk index -> chunk) map, contains all the chunks in the instance.
     */
    private final Long2ObjectMap<Chunk> chunks = new Long2ObjectOpenHashMap<>();
    /**
     * Loaded chunks that are visible to the outside.
     * This is thread-safe for read-only usages.
     */
    private final Long2ObjectSyncMap<Chunk> loadedChunks = Long2ObjectSyncMap.hashmap();
    /**
     * Same as {@link #savingChunks} for instance data
     */
    private boolean savingInstance = false;
    private CompletableFuture<Void> savingInstanceDelayed = null;

    /**
     * TODO in the InstanceContainer code this was volatile. Check why? Other fields like chunkLoader should also have been volatile?
     * the chunk generator used, can be null
     */
    Generator generator;
    /**
     * the chunk loader, used when trying to load/save a chunk from another source
     */
    IChunkLoader chunkLoader;
    /**
     * used to supply a new chunk object at a position when requested
     */
    ChunkSupplier chunkSupplier;
    boolean autosaveEnabled;

    private boolean hasUpdated = false;

    public SingleThreadedManager(TaskSchedulerThread taskSchedulerThread, ChunkAccess chunkAccess) {
        this.taskSchedulerThread = taskSchedulerThread;
        this.chunkAccess = chunkAccess;
        this.chunkWorker = new ChunkWorker(taskSchedulerThread, chunkAccess);
        this.instance = taskSchedulerThread.getInstance();
    }

    @NotNull
    @UnmodifiableView
    Collection<Chunk> loadedChunks() {
        return Collections.unmodifiableCollection(loadedChunks.values());
    }

    @NotNull Chunk loadedChunk(int x, int z) {
        return loadedChunks.get(CoordConversion.chunkIndex(x, z));
    }

    /**
     * This method is responsible for selecting and submitting the chunks to load.
     */
    IterationResult workIteration() {
        // All updates up to here have been seen. We will handle them now.
        this.hasUpdated = false;

        // This loop should not run very long. It will first do any maintenance work (removing stale updates)
        // then it will submit updates as long as the workers can handle these updates, then it will exit,
        // because otherwise the workers would be overloaded
        while (!this.updateQueue.isEmpty()) {
            var update = this.updateQueue.dequeue();
            var result = this.workUpdate(update);
            if (result instanceof UpdateResult.WaitingForWorker) {
                // The worker is busy. Exit loop here
                this.updateQueue.enqueue(update);
                if (this.hasUpdated) {
                    return IterationResult.RUN_AGAIN;
                }
                return IterationResult.WAIT_FOR_SIGNAL_OR_WORKER;
            } else if (result instanceof UpdateResult.WaitingForFuture(CompletableFuture<?> future)) {
                future.whenComplete((o, throwable) -> this.taskSchedulerThread.addTask(new Task.EnqueueUpdate(update)));
            }
        }
        if (this.hasUpdated) {
            return IterationResult.RUN_AGAIN;
        }
        return IterationResult.WAIT_FOR_SIGNAL;
    }

    void enqueue(PrioritizedUpdate update) {
        this.updateQueue.enqueue(update);
    }

    void addClaim(int x, int z, ChunkAndClaim chunkAndClaim) {
        var claim = chunkAndClaim.chunkClaim();
        this.tree.insert(x, z, claim.radius(), claim.priority(), claim.shape());

        var claimedChunk = new TaskSchedulerThread.ClaimedChunk(x, z, chunkAndClaim.chunkFuture());
        var chunkIndex = CoordConversion.chunkIndex(x, z);
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
        this.submitUpdate(x, z, claim.priority(), UpdateType.ADD_CLAIM_EXPLICIT);
    }

    void removeClaim(ChunkClaim claim, CompletableFuture<Void> future) {
        var claimedChunk = this.claimMap.remove(claim);
        if (claimedChunk == null) {
            this.taskSchedulerThread.completeExceptionally(future, new IllegalStateException("The claim you attempted to remove is not valid"));
            return;
        }
        var x = claimedChunk.x();
        var z = claimedChunk.z();
        var chunkIndex = CoordConversion.chunkIndex(x, z);
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
        this.submitUpdate(x, z, claim.priority(), UpdateType.REMOVE_CLAIM_EXPLICIT);
        // We can complete the future right here, the claim was removed.
        // Removing a claim makes no guarantees about when the chunk is unloaded, so this is the easiest
        // and most obvious place to complete the future
        this.taskSchedulerThread.complete(future, null);
        this.taskSchedulerThread.completeExceptionally(claimedChunk.future(), new CancellationException("Claim was removed"));
    }

    void chunkGenerationFinished(Chunk chunk) {
        // The claim may have been removed by now. We will first have to check that
        var x = chunk.getChunkX();
        var z = chunk.getChunkZ();
        var chunkIndex = CoordConversion.chunkIndex(x, z);
        var loadTask = this.loadingChunks.remove(chunkIndex);
        var entry = this.findHighestPriorityEntry(x, z);
        if (entry == null) {
            // The last claim was removed. The chunk should not be loaded, ignore
            return;
        }
        var index = CoordConversion.chunkIndex(chunk.getChunkX(), chunk.getChunkZ());
        this.chunks.put(index, chunk);
        this.chunksLastUpdatePriority.put(index, loadTask.lastUpdatePriority);
        // add this scheduler before adding the dispatcher partition to make sure it is executed first
        this.taskTracking.runningTickScheduledCount.incrementAndGet();
        Runnable task = () -> {
            var old = this.loadedChunks.put(index, chunk);
            List<ClaimedChunk> claimsCopy;
            claimsByChunkLock.lock();
            try {
                var claims = this.claimsByChunk.get(index);
                claimsCopy = List.copyOf(claims);
            } finally {
                claimsByChunkLock.unlock();
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

        if (ServerFlag.INSIDE_TEST) {
            task.run();
        } else {
            chunk.getScheduler().scheduleNextProcess(task);
        }
    }

    void saveChunk(Chunk chunk, CompletableFuture<Void> future) {
        var chunkIndex = CoordConversion.chunkIndex(chunk.getChunkX(), chunk.getChunkZ());
        if (this.savingChunks.containsKey(chunkIndex)) {
            // Already saving, this is a new task.
            // Old task will most likely work on outdated data,
            // so we need to start a new task after the old one finishes
            var saving = this.savingChunksDelayed.get(chunkIndex);
            if (saving != null) {
                // The chunk is already scheduled to be saved again.
                // In this case, we don't need to do anything and can
                // complete the future when saving again has finished.
                link(saving.future(), future);
                return;
            }
            // this is enough to schedule the chunk to be saved again
            this.savingChunksDelayed.put(chunkIndex, new TaskSchedulerThread.SaveTask(chunk, future));
            return;
        }
        this.saveChunk0(new TaskSchedulerThread.SaveTask(chunk, future));
    }

    private void saveChunk0(TaskSchedulerThread.SaveTask saveTask) {
        // For saving with file IO, we use a virtual thread instead of scheduling it to the worker.
        // This way we don't need to reserve a worker, which may otherwise prove to be difficult.
        var chunk = saveTask.chunk();
        var chunkIndex = CoordConversion.chunkIndex(chunk.getChunkX(), chunk.getChunkZ());
        var chunkLoader = this.chunkLoader;
        this.savingChunks.put(chunkIndex, saveTask);
        this.runOnSaveExecutor(() -> {
            try {
                chunkLoader.saveChunk(chunk);
                this.taskSchedulerThread.complete(saveTask.future(), null);
            } catch (Throwable t) {
                this.taskSchedulerThread.completeExceptionally(saveTask.future(), t);
            } finally {
                this.taskSchedulerThread.addTask(new TaskSchedulerThread.Task.SaveChunkCompleted(chunk.getChunkX(), chunk.getChunkZ()));
            }
        });
    }

    void saveChunkCompleted(int x, int z) {
        var chunkIndex = CoordConversion.chunkIndex(x, z);
        if (this.savingChunksDelayed.containsKey(chunkIndex)) {
            var delayedTask = this.savingChunksDelayed.remove(chunkIndex);
            saveChunk0(delayedTask);
            return;
        }
        this.savingChunks.remove(chunkIndex);
    }

    void saveInstanceData(CompletableFuture<Void> future) {
        if (this.savingInstance) {
            if (this.savingInstanceDelayed != null) {
                link(savingInstanceDelayed, future);
                return;
            }
            this.savingInstanceDelayed = future;
            return;
        }
        this.saveInstanceData0(future);
    }

    void saveInstanceData0(CompletableFuture<Void> future) {
        // For saving with file IO, we use a virtual thread instead of scheduling it to the worker.
        // This way we don't need to reserve a worker, which may otherwise prove to be difficult.
        this.savingInstance = true;
        var chunkLoader = this.chunkLoader;
        this.runOnSaveExecutor(() -> {
            try {
                chunkLoader.saveInstance(instance);
                this.taskSchedulerThread.complete(future, null);
            } catch (Throwable t) {
                this.taskSchedulerThread.completeExceptionally(future, t);
            } finally {
                this.taskSchedulerThread.addTask(new TaskSchedulerThread.Task.SaveInstanceDataCompleted());
            }
        });
    }

    void saveInstanceCompleted() {
        if (this.savingInstanceDelayed != null) {
            saveInstanceData0(this.savingInstanceDelayed);
            this.savingInstanceDelayed = null;
            return;
        }
        this.savingInstance = false;
    }

    void saveChunks(CompletableFuture<Void> future) {
        var futures = new CompletableFuture[this.chunks.size()];
        var i = 0;
        for (var chunk : this.chunks.values()) {
            var f = new CompletableFuture<Void>();
            futures[i++] = f;
            this.saveChunk(chunk, f);
        }
        link(CompletableFuture.allOf(futures), future);
    }

    void saveInstanceDataAndChunks(CompletableFuture<Void> future) {
        var chunks = new CompletableFuture<Void>();
        this.saveChunks(chunks);
        var data = new CompletableFuture<Void>();
        this.saveInstanceData(data);
        link(CompletableFuture.allOf(chunks, data), future);
    }

    // Should make control flow more obvious
    @SuppressWarnings({"IfStatementWithIdenticalBranches", "RedundantIfStatement"})
    private boolean shouldIgnoreUpdate(PrioritizedUpdate update) {
        var x = update.x();
        var z = update.z();
        // Check if update priority changed. This could be because a high priority claim was removed.
        var chunkIndex = CoordConversion.chunkIndex(x, z);
        var entry = this.findHighestPriorityEntry(x, z);
        if (entry == null) {
            // ignore if no claim and not loaded, otherwise don't ignore
            return !this.chunks.containsKey(chunkIndex);
        }

        var highestClaimPriority = calculatePriority(entry, x, z);

        if (!this.isValidUpdate(update.priority(), highestClaimPriority)) {
            // Update has different priority than the highest claim, ignore
            return true;
        }

        var chunk = this.chunks.get(chunkIndex);
        if (chunk == null) {
            var loading = this.loadingChunks.get(chunkIndex);
            if (loading == null) {
                // claim exists, no chunk, not loading -> don't ignore, we have to load
                return false;
            }
            if (doubleEqual(loading.lastUpdatePriority, highestClaimPriority)) {
                // already up to date, we can ignore
                return true;
            }
            return false;
        } else {
            if (doubleEqual(this.chunksLastUpdatePriority.get(chunkIndex), highestClaimPriority)) {
                // already up to date, we can ignore
                return true;
            }
            return false;
        }
    }

    /*
    Update logic:

    on claim add/remove -> enqueue update for x,z (priority)

    iteration:

        take the highest priority update

        calcPriority = calculated claim priority (x,z)

        # while the update was in queue, claims may have changed
        if (update priority > calcPriority):
            # claim responsible for update was removed
            next iteration

        if (update priority < calcPriority):
            # update from lower priority claim.
            # this should already have been handled by the update for the higher priority claim, so
            # we can just go next.
            next iteration

        assert update priority ~ calcPriority # account for double rounding errors, use EPSILON comparison

        if (no worker free) {
            wait for signal
            exit iteration
        }

        take all neighbouring chunks with priority < calcPriority:
            enqueue update for neighbour neighbourX,neighbourZ (calculated claim priority for neighbour)

        submit update to worker

     */
    private UpdateResult workUpdate(PrioritizedUpdate update) {
        var x = update.x();
        var z = update.z();
        // Check if update priority changed. This could be because a high priority claim was removed.
        var chunkIndex = CoordConversion.chunkIndex(x, z);
        var entry = this.findHighestPriorityEntry(x, z);
        if (entry == null) {
            return this.updateNoRemainingClaims(chunkIndex, x, z, update);
        }

        var highestClaimPriority = calculatePriority(entry, x, z);

        // Check if the update is still valid. If not, we can ignore it
        if (!this.isValidUpdate(update.priority(), highestClaimPriority)) {
            return UpdateResult.INVALID_UPDATE;
        }

        var chunk = this.chunks.get(chunkIndex);

        // If the chunk is already up to date, we can also ignore this update.
        // This could especially be a problem if a manifold priority drop is used, because
        // updates with the same existing priority will be scheduled and propagated down the line.
        // This can be prevented by saving the last update priority of a chunk
        if (chunk != null) {
            if (this.checkUpToDateLoaded(chunkIndex, highestClaimPriority)) {
                return UpdateResult.ALREADY_UP_TO_DATE;
            }
            // Not already up-to-date, update last priority
            this.chunksLastUpdatePriority.put(chunkIndex, highestClaimPriority);
            // we have to propagate the updates to (potentially) load neighbors
            this.propagateUpdates(entry, highestClaimPriority, x, z, UpdateType.LOAD_PROPAGATE);
            return UpdateResult.UPDATE_PROPAGATED_FOR_LOADED;
        } else {
            if (this.isUpToDateLoading(chunkIndex, highestClaimPriority)) {
                return UpdateResult.ALREADY_UP_TO_DATE;
            }

            // The chunk may already be loaded, only start a worker for unloaded chunks
            return this.updateLoadChunk(chunkIndex, x, z, highestClaimPriority, entry);
        }
    }

    private UpdateResult updateLoadChunk(long chunkIndex, int x, int z, double highestClaimPriority, ChunkClaimTree.CompleteEntry entry) {
        // if we are already loading the chunk, we can just use that task
        var loading = this.loadingChunks.get(chunkIndex);
        if (loading != null) {
            loading.lastUpdatePriority = highestClaimPriority;

            // we have to propagate the updates to (potentially) load neighbors
            this.propagateUpdates(entry, highestClaimPriority, x, z, UpdateType.LOAD_PROPAGATE);

            return UpdateResult.LOAD_SCHEDULED_EXISTING;
        }

        // if we are in the process of unloading/saving the chunk, we can use the cached chunk
        var unloadTask = this.unloadingChunks.get(chunkIndex);
        if (unloadTask != null) {
            return this.updateLoadChunkFromMemory(chunkIndex, x, z, highestClaimPriority, entry, unloadTask);
        }

        if (ChunkWorker.tryReserve()) {

            // we have to propagate the updates to (potentially) load neighbors
            this.propagateUpdates(entry, highestClaimPriority, x, z, UpdateType.LOAD_PROPAGATE);

            var task = new TaskSchedulerThread.LoadTask(this.chunkLoader, this.chunkSupplier, this.generator, x, z, highestClaimPriority);
            this.loadingChunks.put(chunkIndex, task);

            this.submitReserved(() -> {
                try {
                    this.chunkWorker.workerGenerateChunk(task);
                } catch (Throwable throwable) {
                    LOGGER.error("Exception during chunk loading/generation", throwable);
                }
            });
            return UpdateResult.LOAD_SCHEDULED;
        }
        return UpdateResult.WAITING_FOR_WORKER;
    }

    private UpdateResult updateLoadChunkFromMemory(long chunkIndex, int x, int z, double highestClaimPriority, ChunkClaimTree.CompleteEntry entry, UnloadTask unloadTask) {
        // If the partition is not yet deleted, we must wait until it has finished.
        // Otherwise, InstanceChunkLoadEvent/InstanceChunkUnloadEvent may be fired out of order for
        // the same chunk.
        // Example:
        // InstanceChunkLoadEvent - InstanceChunkLoadEvent - InstanceChunkUnloadEvent - InstanceChunkUnloadEvent

        if (unloadTask.partitionDeleted.isDone()) {
            if (ChunkWorker.tryReserve()) {
                var unloading = unloadTask.chunk;
                var task = new TaskSchedulerThread.LoadTask(this.chunkLoader, this.chunkSupplier, this.generator, x, z, highestClaimPriority);
                this.loadingChunks.put(chunkIndex, task);

                // we have to propagate the updates to (potentially) load neighbors
                this.propagateUpdates(entry, highestClaimPriority, x, z, UpdateType.LOAD_PROPAGATE);

                this.submitReserved(() -> {
                    try {
                        this.chunkWorker.workerCopyFromMemory(unloading, x, z);
                    } catch (Throwable throwable) {
                        LOGGER.error("Exception when copying chunk from old (in memory)", throwable);
                    }
                });
                return UpdateResult.LOAD_COPY_SCHEDULED;
            } else {
                return UpdateResult.WAITING_FOR_WORKER;
            }
        } else {
            // This update must wait until the future has completed.
            return new UpdateResult.WaitingForFuture(unloadTask.partitionDeleted);
        }
    }

    private UpdateResult updateNoRemainingClaims(long chunkIndex, int x, int z, PrioritizedUpdate update) {
        // The last claim was removed. We may have to remove the chunk from the chunks map if that hasn't happened already
        var chunk = this.chunks.get(chunkIndex);
        if (chunk != null) {
            this.unloadChunk(chunk);
            // we have to propagate the updates to (potentially) unload neighbors
            this.propagateUpdates(null, update.priority(), x, z, UpdateType.UNLOAD_PROPAGATE);
            return UpdateResult.UNLOAD_SCHEDULED;
        }
        return UpdateResult.INVALID_UPDATE;
    }

    /**
     * Start to unload the chunk.
     * The chunk will be unloaded at some point in the future,
     * but no guarantees are made about when that point is.
     * The only guarantee this method gives is that the chunk
     * will have an entry in {@link #unloadingChunks}
     */
    void unloadChunk(@NotNull Chunk chunk) {
        var x = chunk.getChunkX();
        var z = chunk.getChunkZ();
        var chunkIndex = CoordConversion.chunkIndex(x, z);
        if (this.unloadingChunks.containsKey(chunkIndex)) {
            // TODO?
            return;
        }
        var unloadFuture = new CompletableFuture<Void>();
        var task = new TaskSchedulerThread.UnloadTask(chunk, unloadFuture);
        this.unloadingChunks.put(chunkIndex, task);
        this.chunks.remove(chunkIndex);
        this.chunksLastUpdatePriority.remove(chunkIndex);

        // This future will be completed once the chunk has been saved
        var saveFuture = new CompletableFuture<Void>();

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
            task.partitionDeleted.complete(null);
            this.taskTracking.runningTickScheduledCount.decrementAndGet();
        };
        if (ServerFlag.INSIDE_TEST) {
            runInstance.run();
            runChunk.run();
        } else {
            this.instance.scheduler().scheduleNextProcess(runInstance);
            chunk.getScheduler().scheduleNextProcess(runChunk);
        }

        if (this.autosaveEnabled) {
            this.saveChunk(chunk, saveFuture);
            saveFuture.whenComplete((unused, throwable) -> {
                if (throwable != null) {
                    LOGGER.error("Exception when saving chunk", throwable);
                    return;
                }
                this.taskSchedulerThread.addTask(new Task.FinishUnloadAfterSave(chunk));
            });
        } else {
            this.finishUnloadChunkAfterSave(chunk);
        }
    }

    public void unloadFuture(int x, int z, CompletableFuture<Void> future) {
        var index = CoordConversion.chunkIndex(x, z);
        if (!this.chunks.containsKey(index) && !this.unloadingChunks.containsKey(index)) {
            this.taskSchedulerThread.complete(future, null);
            return;
        }
        var existing = this.unloadFutures.putIfAbsent(index, future);
        if (existing != null) {
            link(existing, future);
        }
    }

    /**
     * Finish unloading after saving has completed
     */
    void finishUnloadChunkAfterSave(@NotNull Chunk chunk) {
        var x = chunk.getChunkX();
        var z = chunk.getChunkZ();
        var chunkIndex = CoordConversion.chunkIndex(x, z);
        var task = this.unloadingChunks.get(chunkIndex);
        if (task.partitionDeleted.isDone()) {
            this.finishUnloadChunkAfterSaveAndPartition(chunk);
        } else {
            task.partitionDeleted.whenComplete((unused, throwable) -> this.taskSchedulerThread.addTask(new Task.FinishUnloadAfterSaveAndPartition(chunk)));
        }
    }

    void finishUnloadChunkAfterSaveAndPartition(@NotNull Chunk chunk) {
        var x = chunk.getChunkX();
        var z = chunk.getChunkZ();
        var chunkIndex = CoordConversion.chunkIndex(x, z);
        this.unloadingChunks.remove(chunkIndex);
        var future = this.unloadFutures.remove(chunkIndex);
        if (future != null) this.taskSchedulerThread.complete(future, null);
    }

    void completeIfLoaded(int x, int z, ChunkAndClaim chunkAndClaim) {
        var index = CoordConversion.chunkIndex(x, z);
        var chunk = this.loadedChunks.get(index);
        if (chunk == null) return;
        this.taskSchedulerThread.complete(chunkAndClaim.chunkFuture(), chunk);
    }

    private @Nullable ChunkClaimTree.CompleteEntry findHighestPriorityEntry(int x, int z) {
        var entries = this.tree.findEntries(x, z);
        if (entries.isEmpty()) return null;
        var comparator = entryComparator(x, z);
        return entries.stream().max(comparator).orElseThrow();
    }

    private void propagateUpdates(@Nullable ChunkClaimTree.CompleteEntry origin, double originPriority, int x, int z, UpdateType updateType) {
        this.propagateUpdate(origin, originPriority, x + 1, z, updateType);
        this.propagateUpdate(origin, originPriority, x - 1, z, updateType);
        this.propagateUpdate(origin, originPriority, x, z + 1, updateType);
        this.propagateUpdate(origin, originPriority, x, z - 1, updateType);
    }

    private void propagateUpdate(@Nullable ChunkClaimTree.CompleteEntry origin, double originPriority, int x, int z, UpdateType updateType) {
        if (origin != null) {
            // propagate load update
            var priority = calculatePriority(origin, x, z);
            if (priority >= originPriority) {
                // updates can't propagate to higher priorities
                return;
            }
            this.submitUpdate(x, z, priority, updateType);
        } else {
            // Propagate unload update. On unload, there is no origin entry, so priority calculation may become difficult.
            // Instead of precise priorities based on a shape, we can just unload in any order, considering this isn't seen
            // by any player and should not be noticeable. Also considering unloads happen before loads, so no new load
            // tasks will be issued until all unloads have finished.
            // Instead of completely random order, we can prioritize by manifold distance, so just originPriority - 1
            this.submitUpdate(x, z, originPriority - 1, updateType);
        }
    }

    private void submitUpdate(int x, int z, double priority, UpdateType updateType) {
        this.updateQueue.enqueue(new PrioritizedUpdate(updateType, priority, x, z));
        this.hasUpdated = true;
    }

    private boolean checkUpToDateLoaded(long chunkIndex, double highestClaimPriority) {
        var lastUpdatePriority = this.chunksLastUpdatePriority.get(chunkIndex);
        // same priority (compared with Vec.EPSILON precision)
        return doubleEqual(highestClaimPriority, lastUpdatePriority);
    }

    private boolean isUpToDateLoading(long chunkIndex, double highestClaimPriority) {
        var loadingChunk = this.loadingChunks.get(chunkIndex);
        if (loadingChunk != null) {
            return doubleEqual(highestClaimPriority, loadingChunk.lastUpdatePriority);
        }
        return false;
    }

    private boolean isValidUpdate(double updatePriority, double highestClaimPriority) {
        if (updatePriority > highestClaimPriority + Vec.EPSILON) {
            // Higher priority claim was removed, lower priority claim remaining for chunk.
            // Ignore this stale update caused by higher priority.
            return false;
        }
        //noinspection RedundantIfStatement makes this more readable, I think
        if (updatePriority < highestClaimPriority - Vec.EPSILON) {
            // update from lower priority claim.
            // this should already have been handled by the update for the higher priority claim, so
            // we can just go next.
            return false;
        }
        return true;
    }

    private void submitReserved(Runnable runnable) {
        this.taskTracking.runningWorkerTaskCount.incrementAndGet();
        ChunkWorker.submitReserved(() -> {
            try {
                runnable.run();
            } finally {
                this.taskTracking.runningWorkerTaskCount.decrementAndGet();
            }
        });
    }

    private void runOnSaveExecutor(Runnable runnable) {
        this.taskTracking.runningSaveTaskCount.incrementAndGet();
        ChunkWorker.runOnSaveExecutor(() -> {
            try {
                runnable.run();
            } finally {
                this.taskTracking.runningSaveTaskCount.decrementAndGet();
            }
        });
    }

    /**
     * Calculates the priority of a given {@code entry} for a chunk at {@code x,z}
     */
    static double calculatePriority(@NotNull ChunkClaimTree.CompleteEntry entry, int x, int z) {
        return entry.entry().priority() - PRIORITY_DROP.calculate(entry.centerX(), entry.centerZ(), x, z);
    }

    static Comparator<ChunkClaimTree.CompleteEntry> entryComparator(int x, int z) {
        return Comparator.comparingDouble(e -> calculatePriority(e, x, z));
    }

    private static boolean doubleEqual(double d1, double d2) {
        return d1 > d2 - Vec.EPSILON && d1 < d2 + Vec.EPSILON;
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

        record WaitingForFuture(CompletableFuture<?> future) implements UpdateResult {
        }
    }
}
