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

import static net.minestom.server.instance.chunksystem.TaskSchedulerThread.Task;
import static net.minestom.server.instance.chunksystem.TaskSchedulerThread.link;

class SingleThreadedManager {
    static InternalCallbacks callbacks = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(SingleThreadedManager.class);
    private final ChunkClaimTree tree = new ChunkClaimTree();
    private final TaskSchedulerThread taskSchedulerThread;
    private final Long2DoubleMap chunksHighestUpdatePriority = new Long2DoubleOpenHashMap();
    private final Long2DoubleMap chunksHighestRadius = new Long2DoubleOpenHashMap();
    private final Long2ObjectMap<LoadTask> loadingChunks = new Long2ObjectOpenHashMap<>();
    /**
     * This HashMap is used to quickly access loaded chunks in case of added claims after they are scheduled for unload.
     * Otherwise, we'd have to wait for the unload/write to finish, and only then could we start the next load.
     * With this, we cache the chunk, and if it is loaded again, we can just copy it and use the new copy.
     * This way we also ensure the chunks are not changed while saving, which may cause issues with the ChunkLoader/-Saver.
     */
    private final Long2ObjectMap<UnloadTask> unloadingChunks = new Long2ObjectOpenHashMap<>();
    private final Long2ObjectMap<CompletableFuture<Void>> unloadFutures = new Long2ObjectOpenHashMap<>();
    /**
     * HashMap for saving chunks. We don't want to save the same chunk concurrently, and we don't want unnecessary saves
     */
    private final Long2ObjectMap<SaveTask> savingChunks = new Long2ObjectOpenHashMap<>();
    /**
     * HashMap to contain all chunks that have their save request delayed. A chunk may be requested for saving
     * while it is being saved already. This save will work on old data though, we want the newest data, so we have
     * to reschedule the save after the first save finishes.
     */
    private final Long2ObjectMap<SaveTask> savingChunksDelayed = new Long2ObjectOpenHashMap<>();
    /**
     * Identity strategy, so we can identify the correct claims and remove them.
     */
    private final Object2ObjectMap<ChunkClaim, ClaimedChunk> claimMap = new Object2ObjectOpenHashMap<>();
    private final ReentrantLock claimsByChunkLock = new ReentrantLock();
    private final Long2ObjectMap<Collection<ClaimedChunk>> claimsByChunk = new Long2ObjectOpenHashMap<>();
    /**
     * A sorted set instead of a priority queue: Duplicate updates can add up quickly and cause OOMEs.
     * Instead of tracking duplicates manually, we can also just use a set, which is more appropriate
     */
    private final ObjectSortedSet<PrioritizedUpdate> updateQueue = new ObjectRBTreeSet<>(PrioritizedUpdate.COMPARATOR);
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
     * The size of the updateQueue after the last update
     */
    private int lastUpdateQueueCleanupSize = 0;
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
        this.chunksHighestUpdatePriority.defaultReturnValue(Double.NaN);
        this.chunksHighestRadius.defaultReturnValue(Double.NaN);
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
            var update = Objects.requireNonNull(this.dequeue());
            var result = this.workUpdate(update);
            if (result instanceof UpdateResult.WaitingForWorker) {
                // The worker is busy. Exit loop here
                enqueue(update);
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
    private UpdateResult workUpdate(@NotNull PrioritizedUpdate update) {
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
//        if (!this.isUpToDate(update.priority(), highestClaimPriority)) {
//            return UpdateResult.INVALID_UPDATE;
//        }
        if (update.priority() - Vec.EPSILON > highestClaimPriority) {
            return UpdateResult.INVALID_UPDATE;
        }

        var chunk = this.chunks.get(chunkIndex);
        var highestRadius = this.chunksHighestRadius.get(chunkIndex);
        var highestUpdatePriority = this.chunksHighestUpdatePriority.get(chunkIndex);

        // If the chunk is already up to date, we can also ignore this update.
        // This could especially be a problem if a manifold priority drop is used, because
        // updates with the same existing priority will be scheduled and propagated down the line.
        // This can be prevented by saving the last update priority of a chunk
//        if (this.isUpToDate(chunkIndex, highestClaimPriority)) {
//            return UpdateResult.ALREADY_UP_TO_DATE;
//        }
        if (chunk != null) {
            if (highestClaimPriority + Vec.EPSILON > highestUpdatePriority && highestRadius >= update.radius()) {
                return UpdateResult.ALREADY_UP_TO_DATE;
            }
            // Not already up-to-date, update last priority
            this.chunksHighestUpdatePriority.put(chunkIndex, highestClaimPriority);
            this.chunksHighestRadius.put(chunkIndex, Math.max(highestRadius, update.radius()));
            // we have to propagate the updates to (potentially) load neighbors
            this.propagateUpdates(update, entry, highestClaimPriority, UpdateType.LOAD_PROPAGATE);
            return UpdateResult.UPDATE_PROPAGATED_FOR_LOADED;
        } else {
            // The chunk may already be loading, only start a worker for fully unloaded chunks
            return this.updateLoadChunk(update, highestClaimPriority, entry);
        }
    }

    private void cleanupUpdateQueue() {
        var removed = new ArrayList<PrioritizedUpdate>();
        var temporary = new ArrayList<PrioritizedUpdate>();
        while (!this.updateQueue.isEmpty()) {
            var update = Objects.requireNonNull(dequeue());
            var x = update.x();
            var z = update.z();
            var chunkIndex = CoordConversion.chunkIndex(x, z);
            var entry = this.findHighestPriorityEntry(x, z);
            if (entry == null) {
                removed.add(update);
                // TODO
                continue;
            }
            var highestClaimPriority = calculatePriority(entry, x, z);
            if (!this.isUpToDate(update.priority(), highestClaimPriority)) {
                removed.add(update);
                continue;
            }
            if (this.isUpToDate(chunkIndex, highestClaimPriority)) {
                removed.add(update);
                continue;
            }
            var chunk = this.chunks.get(chunkIndex);
            if (chunk != null) {
                temporary.add(update);
                continue;
            }
            // check if the chunk should load
            var loadTask = this.loadingChunks.get(chunkIndex);
            if (loadTask != null) {
                temporary.add(update);
                continue;
            }
            temporary.add(update);
        }
        for (var update : temporary) {
            enqueue(update);
        }
//        if (callbacks != null) {
//            for (var update : removed) {
//                callbacks.removeUpdate(update.x(), update.z(), update.updateType());
//            }
//        }
    }

    void enqueue(PrioritizedUpdate update) {
        if (!this.updateQueue.add(update)) return;

        if (callbacks != null) {
            callbacks.addUpdate(update.x(), update.z(), update.updateType());
        }

        if (this.updateQueue.size() > (this.lastUpdateQueueCleanupSize << 2) + 100) {
            // We use this formula to make sure we don't do updates too often.
//            cleanupUpdateQueue(); TODO
            this.lastUpdateQueueCleanupSize = this.updateQueue.size();
        }
    }

    private @Nullable PrioritizedUpdate dequeue() {
        if (this.updateQueue.isEmpty()) return null;
        var update = this.updateQueue.removeFirst();
        if (callbacks != null) {
            callbacks.removeUpdate(update.x(), update.z(), update.updateType());
        }
        return update;
    }

    void addClaim(int x, int z, ChunkAndClaim chunkAndClaim) {
        var claim = chunkAndClaim.chunkClaim();
        this.tree.insert(x, z, claim.radius(), claim.priority(), claim.shape());

        var claimedChunk = new ClaimedChunk(x, z, chunkAndClaim.chunkFuture(), new LongOpenHashSet());
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
        var radius = claim.radius();
        this.submitUpdate(x, z, radius, claim.priority(), UpdateType.ADD_CLAIM_EXPLICIT);

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
        var radius = claim.radius();
        this.submitUpdate(x, z, radius, claim.priority(), UpdateType.REMOVE_CLAIM_EXPLICIT);
        // We can complete the future right here, the claim was removed.
        // Removing a claim makes no guarantees about when the chunk is unloaded, so this is the easiest
        // and most obvious place to complete the future
        this.taskSchedulerThread.complete(future, null);
        this.taskSchedulerThread.completeExceptionally(claimedChunk.future(), new CancellationException("Claim was removed"));

        if (callbacks != null) {
            callbacks.onRemoveClaim(x, z, claim);
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
            this.savingChunksDelayed.put(chunkIndex, new SaveTask(chunk, future));
            return;
        }
        this.saveChunk0(new SaveTask(chunk, future));
    }

    private void saveChunk0(SaveTask saveTask) {
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

    // TODO maybe see if we want to do some kind of update queue maintenance in the future
    //  Would have to update/revisit this logic though

    /// / Should make control flow more obvious
    //@SuppressWarnings({"IfStatementWithIdenticalBranches", "RedundantIfStatement"})
    //private boolean shouldIgnoreUpdate(PrioritizedUpdate update) {
    //    var x = update.x();
    //    var z = update.z();
    //    // Check if update priority changed. This could be because a high priority claim was removed.
    //    var chunkIndex = CoordConversion.chunkIndex(x, z);
    //    var entry = this.findHighestPriorityEntry(x, z);
    //    if (entry == null) {
    //        // ignore if no claim and not loaded, otherwise don't ignore
    //        return !this.chunks.containsKey(chunkIndex);
    //    }
    //
    //    var highestClaimPriority = calculatePriority(entry, x, z);
    //
    //    if (!this.isValidUpdate(update.priority(), highestClaimPriority)) {
    //        // Update has different priority than the highest claim, ignore
    //        return true;
    //    }
    //
    //    var chunk = this.chunks.get(chunkIndex);
    //    if (chunk == null) {
    //        var loadTask = this.loadingChunks.get(chunkIndex);
    //        if (loadTask == null) {
    //            // claim exists, no chunk, not loading -> don't ignore, we have to load
    //            return false;
    //        }
    //        if (doubleEqual(loadTask.lastUpdatePriority, highestClaimPriority)) {
    //            // already up to date, we can ignore
    //            return true;
    //        }
    //        return false;
    //    } else {
    //        if (doubleEqual(this.chunksLastUpdatePriority.get(chunkIndex), highestClaimPriority)) {
    //            // already up to date, we can ignore
    //            return true;
    //        }
    //        return false;
    //    }
    //}
    private UpdateResult updateLoadChunk(PrioritizedUpdate update, double highestClaimPriority, ChunkClaimTree.CompleteEntry entry) {
        var x = update.x();
        var z = update.z();
        var chunkIndex = CoordConversion.chunkIndex(x, z);
        // if we are already loading the chunk, we can just use that task
        var loading = this.loadingChunks.get(chunkIndex);
        if (loading != null) {
            this.chunksHighestUpdatePriority.put(chunkIndex, highestClaimPriority);

            // we have to propagate the updates to (potentially) load neighbors
            this.propagateUpdates(update, entry, highestClaimPriority, UpdateType.LOAD_PROPAGATE);

            return UpdateResult.LOAD_SCHEDULED_EXISTING;
        }

        // if we are in the process of unloading/saving the chunk, we can use the cached chunk
        var unloadTask = this.unloadingChunks.get(chunkIndex);
        if (unloadTask != null) {
            return this.updateLoadChunkFromMemory(update, highestClaimPriority, entry, unloadTask);
        }

        if (ChunkWorker.tryReserve()) {

            // we have to propagate the updates to (potentially) load neighbors
            this.propagateUpdates(update, entry, highestClaimPriority, UpdateType.LOAD_PROPAGATE);

            this.startLoad(chunkIndex, x, z, highestClaimPriority);

            this.runOnWorkerWithReservation(() -> {
                if (callbacks != null) {
                    callbacks.onGenerationStarted(x, z);
                }
                try {
                    this.chunkWorker.workerGenerateChunk(x, z);
                } catch (Throwable throwable) {
                    LOGGER.error("Exception during chunk loading/generation", throwable);
                }
            });
            return UpdateResult.LOAD_SCHEDULED;
        }
        return UpdateResult.WAITING_FOR_WORKER;
    }

    private void startLoad(long chunkIndex, int x, int z, double highestClaimPriority) {
        this.chunksHighestUpdatePriority.put(chunkIndex, highestClaimPriority);
        var task = new LoadTask(this.chunkLoader, this.chunkSupplier, this.generator, x, z);
        this.loadingChunks.put(chunkIndex, task);
        if (callbacks != null) {
            callbacks.onLoadStarted(x, z);
        }
    }

    private UpdateResult updateLoadChunkFromMemory(PrioritizedUpdate update, double highestClaimPriority, ChunkClaimTree.CompleteEntry entry, UnloadTask unloadTask) {
        // If the partition is not yet deleted, we must wait until it has finished.
        // Otherwise, InstanceChunkLoadEvent/InstanceChunkUnloadEvent may be fired out of order for
        // the same chunk.
        // Example:
        // InstanceChunkLoadEvent - InstanceChunkLoadEvent - InstanceChunkUnloadEvent - InstanceChunkUnloadEvent
        var x = update.x();
        var z = update.z();
        var chunkIndex = CoordConversion.chunkIndex(x, z);
        if (unloadTask.partitionDeleted.isDone()) {
            if (ChunkWorker.tryReserve()) {
                var unloading = unloadTask.chunk;

                this.startLoad(chunkIndex, x, z, highestClaimPriority);

                // we have to propagate the updates to (potentially) load neighbors
                this.propagateUpdates(update, entry, highestClaimPriority, UpdateType.LOAD_PROPAGATE);


                this.runOnWorkerWithReservation(() -> {
                    if (callbacks != null) {
                        callbacks.onGenerationStarted(x, z);
                    }
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
        if (true) return UpdateResult.INVALID_UPDATE;
        // The last claim was removed. We may have to remove the chunk from the chunks map if that hasn't happened already
        var chunk = this.chunks.get(chunkIndex);
        var radius = update.radius() - 1;
        if (chunk != null) {
            this.unloadChunk(chunk);
            // we have to propagate the updates to (potentially) unload neighbors
            this.propagateUpdates(update, null, update.priority(), UpdateType.UNLOAD_PROPAGATE);
            return UpdateResult.UNLOAD_SCHEDULED;
        }
        var loadTask = this.loadingChunks.get(chunkIndex);
        if (loadTask != null) {
            var lastUpdatePriority = this.chunksHighestUpdatePriority.get(chunkIndex);
            if (Double.isNaN(lastUpdatePriority)) {
                return UpdateResult.INVALID_UPDATE;
            }
            // Set to NaN to signal absence of priority.
            this.chunksHighestUpdatePriority.remove(chunkIndex);

            // we have to propagate the updates to (potentially) unload neighbors
            this.propagateUpdates(update, null, update.priority(), UpdateType.UNLOAD_PROPAGATE);
            return UpdateResult.UNLOAD_SCHEDULED;
        }
        return UpdateResult.INVALID_UPDATE;
    }

    void chunkGenerationFinished(Chunk chunk) {
        // The claim may have been removed by now. We will first have to check that
        var x = chunk.getChunkX();
        var z = chunk.getChunkZ();
        var chunkIndex = CoordConversion.chunkIndex(x, z);
        this.loadingChunks.remove(chunkIndex);
        if (callbacks != null) {
            callbacks.onGenerationCompleted(x, z);
        }
        if (!this.chunksHighestUpdatePriority.containsKey(chunkIndex)) {
            // This chunk should not be loaded. NaN signals that the update was removed.
            // The loadTask still exists in case we add another update to load this again.
            // We are already doing the work; if we end up needing it no reason to throw it away.
            if (callbacks != null) {
                callbacks.onLoadCancelled(x, z);
            }
            return;
        }
        var entry = this.findHighestPriorityEntry(x, z);
        if (entry == null) {
            // The last claim was removed. The chunk should not be loaded, ignore
            if (callbacks != null) {
//                System.out.println("Idk shouldn't happen"); // TODO
                callbacks.onLoadCancelled(x, z);
            }
            return;
        }
        if (callbacks != null) {
            callbacks.onLoadCompleted(x, z);
        }
        this.chunks.put(chunkIndex, chunk);
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
        var task = new UnloadTask(chunk, unloadFuture, new CompletableFuture<>());
        this.unloadingChunks.put(chunkIndex, task);
        this.chunks.remove(chunkIndex);
        this.chunksHighestUpdatePriority.remove(chunkIndex);

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
            task.partitionDeleted.complete(null);
            this.taskSchedulerThread.addTask(new Task.FinishUnloadAfterPartition(chunk));
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

    void finishUnloadAfterPartition(Chunk chunk) {
        if (this.autosaveEnabled) {
            // This future will be completed once the chunk has been saved
            var saveFuture = new CompletableFuture<Void>();

            this.saveChunk(chunk, saveFuture);
            saveFuture.whenComplete((unused, throwable) -> {
                if (throwable != null) {
                    LOGGER.error("Exception when saving chunk", throwable);
                    return;
                }
                this.taskSchedulerThread.addTask(new Task.FinishUnloadAfterSaveAndPartition(chunk));
            });
        } else {
            this.finishUnloadChunkAfterSaveAndPartition(chunk);
        }
    }

    void unloadFuture(int x, int z, CompletableFuture<Void> future) {
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

    void finishUnloadChunkAfterSaveAndPartition(@NotNull Chunk chunk) {
        var x = chunk.getChunkX();
        var z = chunk.getChunkZ();
        var chunkIndex = CoordConversion.chunkIndex(x, z);
        this.unloadingChunks.remove(chunkIndex);
        var future = this.unloadFutures.remove(chunkIndex);
        if (future != null) {
            this.taskSchedulerThread.complete(future, null);
        }

        if (!this.chunksHighestUpdatePriority.containsKey(chunkIndex)) {
            if (callbacks != null) {
                callbacks.onUnloadCompleted(x, z);
            }
        }
    }

    void completeIfLoaded(int x, int z, ChunkAndClaim chunkAndClaim) {
        var index = CoordConversion.chunkIndex(x, z);
        var chunk = this.loadedChunks.get(index);
        if (chunk == null) return;
        this.taskSchedulerThread.complete(chunkAndClaim.chunkFuture(), chunk);
    }

    private void submitUpdate(int x, int z, int radius, double priority, UpdateType updateType) {
        this.enqueue(new PrioritizedUpdate(updateType, priority, x, z, radius));
        this.hasUpdated = true;
        if (this.updateQueue.size() > 50000) {
            return;
        }
    }

    private boolean isUpToDate(long chunkIndex, double highestClaimPriority) {
        var lastUpdatePriority = this.chunksHighestUpdatePriority.get(chunkIndex);
        return this.isUpToDate(highestClaimPriority, lastUpdatePriority);
    }

    private boolean isUpToDate(double updatePriority, double highestClaimPriority) {
        // An update is valid if both priorities are the same.
        // Updates with differing priority mean they are stale;
        // they have been too long in the queue, and the claim has changed.
        // TODO radius checking - if another claim has same priority but higher radius,
        //  and update has not yet propagated for that claim. Update would never propagate
        //  if we don't do radius checking
        return doubleEqual(updatePriority, highestClaimPriority);
    }

    private @Nullable ChunkClaimTree.CompleteEntry findHighestPriorityEntry(int x, int z) {
        var entries = this.tree.findEntries(x, z);
        if (entries.isEmpty()) return null;
        var comparator = entryPriorityComparator(x, z);
        return entries.stream().max(comparator).orElseThrow();
    }

    private void runOnWorkerWithReservation(Runnable runnable) {
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

    private void propagateUpdate(@Nullable ChunkClaimTree.CompleteEntry origin, double originPriority, int x, int z, int radius, UpdateType updateType) {
        if (origin != null) {
            assert updateType == UpdateType.LOAD_PROPAGATE;
            // propagate load update
            var priority = calculatePriority(origin, x, z);
            if (priority + Vec.EPSILON >= originPriority) {
                // updates can't propagate to higher priorities
                return;
            }
            if (this.tree.noEntries(x, z)) {
                // update is unnecessary
                return;
            }
            this.submitUpdate(x, z, radius, priority, updateType);
        } else {
            assert updateType == UpdateType.UNLOAD_PROPAGATE;
            // Propagate unload update. On unload, there is no origin entry, so priority calculation may become difficult.
            // Instead of precise priorities based on a shape, we can just unload in any order, considering this isn't seen
            // by any player and should not be noticeable. Also considering unloads happen before loads, so no new load
            // tasks will be issued until all unloads have finished.
            // Instead of completely random order, we can prioritize by manifold distance, so just originPriority - 1
            this.submitUpdate(x, z, radius, originPriority + 1, updateType);
        }
    }

    /**
     * originPriority is not the priority of the update. Another update with higher priority and smaller radius could exist
     */
    private void propagateUpdates(@NotNull PrioritizedUpdate originUpdate, @Nullable ChunkClaimTree.CompleteEntry origin, double originPriority, UpdateType updateType) {
        var radius = originUpdate.radius() - 1;
        var x = originUpdate.x();
        var z = originUpdate.z();
        this.propagateUpdate(origin, originPriority, x + 1, z + 1, radius, updateType);
        this.propagateUpdate(origin, originPriority, x + 1, z, radius, updateType);
        this.propagateUpdate(origin, originPriority, x + 1, z - 1, radius, updateType);
        this.propagateUpdate(origin, originPriority, x, z - 1, radius, updateType);
        this.propagateUpdate(origin, originPriority, x - 1, z - 1, radius, updateType);
        this.propagateUpdate(origin, originPriority, x - 1, z, radius, updateType);
        this.propagateUpdate(origin, originPriority, x - 1, z + 1, radius, updateType);
        this.propagateUpdate(origin, originPriority, x, z + 1, radius, updateType);
    }

    /**
     * Calculates the priority of a given {@code entry} for a chunk at {@code x,z}
     */
    private double calculatePriority(@NotNull ChunkClaimTree.CompleteEntry entry, int x, int z) {
        return entry.entry().priority() - priorityDrop.calculate(entry.centerX(), entry.centerZ(), x, z);
    }

    private Comparator<ChunkClaimTree.CompleteEntry> entryPriorityComparator(int x, int z) {
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

    record LoadTask(@NotNull IChunkLoader loader, @NotNull ChunkSupplier chunkSupplier, @Nullable Generator generator,
                    int x, int z) {
    }

    private record UnloadTask(@NotNull Chunk chunk, CompletableFuture<Void> future,
                              CompletableFuture<Void> partitionDeleted) {
    }

    private record SaveTask(@NotNull Chunk chunk, @NotNull CompletableFuture<Void> future) {
    }

    private record ClaimedChunk(int x, int z, CompletableFuture<Chunk> future, LongSet dependingChunks) {
    }
}
