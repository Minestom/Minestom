package net.minestom.server.instance.chunksystem.impl;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.ObjectHeapPriorityQueue;
import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.instance.InstanceTickEvent;
import net.minestom.server.instance.*;
import net.minestom.server.instance.chunksystem.ChunkAndClaim;
import net.minestom.server.instance.chunksystem.ChunkClaim;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.utils.chunk.ChunkSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.vectrix.flare.fastutil.Long2ObjectSyncMap;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class is responsible for managing the chunk claims.
 * <p>
 * Because the tree is not thread safe, this class distributes chunk tasks and takes in orders
 * in a thread safe manner.
 * <p>
 * Some of the key concepts of this system are:
 * <ul>
 *     <li>
 *         <b>Lazy updates</b>
 *         <p>
 *             All updates are lazy. This means if a player joins the world with 32 chunk view distance
 *             (a claim is added with 32 radius), the system won't immediately schedule load tasks for all
 *             32 chunks and instead load/unload chunks lazily / on-demand based on priority.
 *         </p>
 *     </li>
 *     <li>
 *         <b>Explicit before implicit updates</b>
 *         <p>
 *             Explicit updates are always handled before implicit updates. This makes chunk claims
 *             faster, because a high priority large radius claim won't stall a lower priority claim.
 *             First the chunks of those claims will be loaded/unloaded, then all implicit chunks originating
 *             from those claims.
 *         </p>
 *     </li>
 *     <li>
 *         <b>Unloads before loads</b><br>
 *         <p>
 *             It is important for all unloads to happen before any new loads. If that was not the case,
 *             when a player moves away from a fully loaded area, the far chunks are unloaded with low priority.
 *             If the player keeps moving and issues new loads with higher priority, because they are closer to
 *             the player, then the far chunks will never be unloaded.
 *         </p>
 *         <p>
 *             A side effect of this is that unloads will be able to stall new loads. Consider a player leaving
 *             the instance. All chunks in the view distance of that player will have to be unloaded before any
 *             new chunks can be loaded. This can cause "lag" during loading of those new chunks.
 *         </p>
 *         <p>
 *             <b>NOTE</b>: In the future some more complex logic could be implemented, like unloads first, but after
 *             two (or X amount of) unloads we see if a load update exists, if so then execute the load update,
 *             continue with unloads after, repeat.
 *             With that approach loads would still happen, but unloads remain (somewhat) prioritized.
 *             This would be more complex though and as of now is not deemed necessary.
 *         </p>
 *     </li>
 *     <li>
 *         <b>Read-Only chunk saving</b>
 *         <p>
 *             Chunk saving is expected to be read-only. When unloading a chunk it has to be saved. But when, during
 *             saving, the chunk should be loaded again we run into issues. If the saving has not yet finished, we'd
 *             load incorrect/incomplete/old data. For this reason chunks in the process of being unloaded are cached,
 *             and if a chunk should be loaded and is in the cache, a copy of the cached chunk will be created and
 *             used instead of the ChunkLoader.
 *         </p>
 *         <p>
 *             The alternative to this is waiting for saving to be complete, which is actually not that simple to
 *             implement without wasting work, and it is guaranteed to be slower than a simple memory copy.
 *         </p>
 *     </li>
 * </ul>
 */
public class ChunkClaimManager implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChunkClaimManager.class);
    private static final Hash.Strategy<ChunkClaim> IDENTITY = new Hash.Strategy<>() {
        @Override
        public int hashCode(ChunkClaim o) {
            return System.identityHashCode(o);
        }

        @Override
        public boolean equals(ChunkClaim a, ChunkClaim b) {
            return a == b;
        }
    };
    /**
     * TODO we could also make this per-instance configurable
     */
    private static final PriorityDrop PRIORITY_DROP = switch (ServerFlag.CHUNK_SYSTEM_PRIORITY_DROP) {
        case "simple" -> new PriorityDrop.Simple();
        case "hypotenuse" -> new PriorityDrop.Hypotenuse();
        case null, default -> new PriorityDrop.HypotenuseSquared();
    };
    /**
     * Use a common worker pool for all managers. A manager may only submit a task if he holds
     * a permit in {@link #AVAILABLE_TASKS}
     */
    private static final ExecutorService WORKER_EXECUTOR;
    /**
     * We allow twice the amount of available processors to be submitted before waiting.
     * This is so we don't waste time.
     * As soon as a task finishes, the thread will be able to take the next task,
     * which has already been submitted.
     */
    private static final Semaphore AVAILABLE_TASKS = new Semaphore(Runtime.getRuntime().availableProcessors() * 2);

    private final CompletableFuture<Void> shutdownFuture = new CompletableFuture<>();
    private final ChunkClaimTree tree = new ChunkClaimTree();
    /**
     * (chunk index -> chunk) map, contains all the chunks in the instance
     */
    private final Long2ObjectSyncMap<Chunk> chunks = Long2ObjectSyncMap.hashmap();
    private final Long2DoubleMap chunksLastUpdatePriority = new Long2DoubleOpenHashMap();
    private final Long2ObjectMap<LoadTask> loadingChunks = new Long2ObjectOpenHashMap<>();
    /**
     * This HashMap is used to quickly access loaded chunks in case of added claims after they are scheduled for unload.
     * Otherwise, we'd have to wait for the unload/write to finish, and only then could we start the next load.
     * With this, we cache the chunk, and if it is loaded again, we can just copy it and use the new copy.
     * This way we also ensure the chunks are not changed while saving, which may cause issues with the ChunkLoader/-Saver.
     */
    private final Long2ObjectMap<Chunk> unloadingChunks = new Long2ObjectOpenHashMap<>();
    /**
     * Identity strategy so we can identify the correct claims and remove them.
     */
    private final Object2ObjectMap<ChunkClaim, ClaimedChunk> claimMap = new Object2ObjectOpenCustomHashMap<>(IDENTITY);
    private final Long2ObjectMap<Collection<ClaimedChunk>> claimsByChunk = new Long2ObjectOpenHashMap<>();
    private final ObjectHeapPriorityQueue<PrioritizedUpdate> updateQueue = new ObjectHeapPriorityQueue<>(PrioritizedUpdate.COMPARATOR);
    private final Instance instance;
    private final ChunkGenerationHandler chunkGenerationHandler;
    private final ConcurrentLinkedQueue<Task> tasks = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<Runnable> instanceTasks = new ConcurrentLinkedQueue<>();
    private final ReentrantLock signalLock = new ReentrantLock();
    private final Condition hasSignalled = this.signalLock.newCondition();
    private final ChunkAccess chunkAccess;
    /**
     * May only ever be written to from inside the lock, may be read from anywhere.
     * This is volatile, because we expect a high amount of reading with very little writing.
     * This allows us to optimize signalling to not need to lock in some situations.
     */
    private volatile boolean signalled = false;
    private volatile boolean exit = false;

    /**
     * TODO in the InstanceContainer code this was volatile. Check why? Other fields like chunkLoader should also have been volatile?
     * the chunk generator used, can be null
     */
    private Generator generator;
    /**
     * the chunk loader, used when trying to load/save a chunk from another source
     */
    private IChunkLoader chunkLoader;
    /**
     * used to supply a new chunk object at a position when requested
     */
    private ChunkSupplier chunkSupplier;
    private boolean autosaveEnabled;
    private boolean hasUpdated = false;

    public ChunkClaimManager(@NotNull Instance instance, @Nullable ChunkSupplier chunkSupplier, @Nullable IChunkLoader chunkLoader, ChunkAccess chunkAccess) {
        this.instance = instance;
        this.chunkAccess = chunkAccess;
        this.chunkGenerationHandler = new ChunkGenerationHandler(instance);
        this.setChunkLoader(chunkLoader);
        this.setChunkSupplier(Objects.requireNonNullElse(chunkSupplier, DynamicChunk::new));
        this.chunkLoader.loadInstance(instance);
        // we finally start ourselves. Now we start accepting tasks. Virtual thread is appropriate here, we
        // will spend most time waiting for signals/workers
        Thread.startVirtualThread(this);
        instance.eventNode().addListener(InstanceTickEvent.class, event -> instanceTick());
    }

    private void instanceTick() {
        while (true) {
            var task = instanceTasks.poll();
            if (task == null) break;
            task.run();
        }
    }

    private void runOnInstance(Runnable runnable) {
        instanceTasks.offer(runnable);
    }

    @Override
    public void run() {
        while (!this.exit) {
            this.signalLock.lock();
            try {
                // set signalled to false, all updates up to here will have been picked up
                this.signalled = false;
            } finally {
                this.signalLock.unlock();
            }
            while (true) {
                var task = this.tasks.poll();
                if (task == null) break;
                this.workTask(task);
            }
            this.workIteration();
            if (this.hasUpdated) continue;
            // check signalled, if we have already been signalled we can avoid having to lock
            if (!this.signalled) {
                this.signalLock.lock();
                try {
                    // re-check condition, may have changed since locking, and we don't want to deadlock
                    if (!this.signalled) {
                        try {
                            // TODO we could also use awaitUninterruptibly, but should we?
                            this.hasSignalled.await();
                        } catch (InterruptedException e) {
                            LOGGER.error("Unexpected interrupt. Someone is meddling with the ChunkClaimManager, this is not allowed! Shutting ChunkClaimManager down!", e);
                            break;
                        }
                    }
                } finally {
                    this.signalLock.unlock();
                }
            }
        }
        // after we shut down normally, we shouldn't have to process the remaining tasks.
        // TODO check in the future if this assumption holds
    }

    public @NotNull IChunkLoader getChunkLoader() {
        return chunkLoader;
    }

    public void setChunkLoader(@Nullable IChunkLoader chunkLoader) {
        this.chunkLoader = Objects.requireNonNullElse(chunkLoader, IChunkLoader.noop());
    }

    public @NotNull ChunkSupplier getChunkSupplier() {
        return chunkSupplier;
    }

    public void setChunkSupplier(@NotNull ChunkSupplier chunkSupplier) {
        this.chunkSupplier = chunkSupplier;
    }

    public boolean isAutosaveEnabled() {
        return autosaveEnabled;
    }

    public void setAutosaveEnabled(boolean autosaveEnabled) {
        this.autosaveEnabled = autosaveEnabled;
    }

    public @Nullable Chunk getLoadedChunk(int chunkX, int chunkZ) {
        return this.chunks.get(CoordConversion.chunkIndex(chunkX, chunkZ));
    }

    private void workTask(Task task) {
        switch (task) {
            case Task.AddClaim addClaim -> workAddClaim(addClaim.x, addClaim.z, addClaim.chunkAndClaim);
            case Task.RemoveClaim removeClaim -> workRemoveClaim(removeClaim.claim, removeClaim.future);
            case Task.ChunkGenerationFinished chunkGenerationFinished ->
                    workChunkGenerationFinished(chunkGenerationFinished.chunk);
        }
    }

    private void workAddClaim(int x, int z, ChunkAndClaim chunkAndClaim) {
        var claim = chunkAndClaim.chunkClaim();
        this.tree.insert(x, z, claim.radius(), claim.priority(), claim.shape());

        var claimedChunk = new ClaimedChunk(x, z, chunkAndClaim.chunkFuture());
        var chunkIndex = CoordConversion.chunkIndex(x, z);
        this.claimMap.put(claim, claimedChunk);
        this.claimsByChunk.computeIfAbsent(chunkIndex, c -> new HashSet<>(4)).add(claimedChunk);

        // if the chunk is already loaded we can complete the future right here.
        // The new claim has been inserted into the map, the chunk may not be unloaded
        // as long as the claim exists, so there shouldn't be any issues with this.
        this.completeIfLoaded(x, z, chunkAndClaim);
        this.submitUpdate(x, z, claim.priority(), UpdateType.ADD_CLAIM_EXPLICIT);
    }

    private void workRemoveClaim(ChunkClaim claim, CompletableFuture<Void> future) {
        var claimedChunk = this.claimMap.remove(claim);
        if (claimedChunk == null) {
            this.completeExceptionally(future, new IllegalStateException("The claim you attempted to remove is not valid"));
            return;
        }
        var x = claimedChunk.x();
        var z = claimedChunk.z();
        var chunkIndex = CoordConversion.chunkIndex(x, z);
        var claims = this.claimsByChunk.get(chunkIndex);
        claims.remove(claimedChunk);
        if (claims.isEmpty()) {
            this.claimsByChunk.remove(chunkIndex);
        }

        this.tree.delete(x, z, claim.radius(), claim.priority(), claim.shape());
        this.submitUpdate(x, z, claim.priority(), UpdateType.REMOVE_CLAIM_EXPLICIT);
        // we can complete the future right here, the claim was removed.
        // Removing a claim makes no guarantees about when the chunk is unloaded, so this is the easiest
        // and most obvious place to complete the future
        this.complete(future, null);
        this.completeExceptionally(claimedChunk.future(), new CancellationException("Claim was removed"));
    }

    private void workChunkGenerationFinished(Chunk chunk) {
        // the claim may have been removed by now. We will first have to check that
        var x = chunk.getChunkX();
        var z = chunk.getChunkZ();
        var chunkIndex = CoordConversion.chunkIndex(x, z);
        var loadTask = this.loadingChunks.remove(chunkIndex);
        var entry = this.findHighestPriorityEntry(x, z);
        if (entry == null) {
            // last claim was removed. The chunk should not be loaded, ignore
            return;
        }
        var index = CoordConversion.chunkIndex(chunk.getChunkX(), chunk.getChunkZ());
        this.chunks.put(index, chunk);
        this.chunksLastUpdatePriority.put(index, loadTask.lastUpdatePriority);
        this.runOnInstance(() -> cacheChunk(chunk));
    }

    private void cacheChunk(Chunk chunk) {
        var dispatcher = MinecraftServer.process().dispatcher();
        dispatcher.createPartition(chunk);
    }

    private void uncacheChunk(Chunk chunk) {
        this.instance.getEntityTracker().chunkEntities(chunk.getChunkX(), chunk.getChunkZ(), EntityTracker.Target.ENTITIES).forEach(Entity::remove);
        var loadedChunk = this.chunks.remove(CoordConversion.chunkIndex(chunk.getChunkX(), chunk.getChunkZ()));
        // TODO
    }

    private void propagateUpdates(@Nullable ChunkClaimTree.CompleteEntry origin, double originPriority, int x, int z, UpdateType updateType) {
        propagateUpdate(origin, originPriority, x + 1, z, updateType);
        propagateUpdate(origin, originPriority, x - 1, z, updateType);
        propagateUpdate(origin, originPriority, x, z + 1, updateType);
        propagateUpdate(origin, originPriority, x, z - 1, updateType);
    }

    private void propagateUpdate(@Nullable ChunkClaimTree.CompleteEntry origin, double originPriority, int x, int z, UpdateType updateType) {
        if (origin != null) {
            // propagate load update
            var priority = calculatePriority(origin, x, z);
            if (priority >= originPriority) {
                // updates can't propagate to higher priorities
                return;
            }
            submitUpdate(x, z, priority, updateType);
        } else {
            // propagate unload update. On unload, there is no origin entry, so priority calculation may become difficult.
            // instead of precise priorities based on a shape, we can just unload in any order, considering this isn't seen
            // by any player and should not be noticeable. Also considering unloads happen before loads, so no new load
            // tasks will be issued until all unloads have finished.
            // Instead of completely random order, we can prioritize by manifold distance, so just originPriority - 1
            submitUpdate(x, z, originPriority - 1, updateType);
        }
    }

    private void submitUpdate(int x, int z, double priority, UpdateType updateType) {
        this.updateQueue.enqueue(new PrioritizedUpdate(updateType, priority, x, z));
        this.hasUpdated = true;
    }

    /**
     * This method is responsible for selecting and submitting the chunks to load.
     */
    private void workIteration() {
        // All updates up to here have been seen. We will handle them now.
        this.hasUpdated = false;

        // this loop should not run very long. It will first do any maintenance work (removing stale updates)
        // then it will submit updates as long as the workers can handle these updates, then it will exit,
        // because otherwise the workers would be overloaded
        while (!this.updateQueue.isEmpty()) {
            var update = this.updateQueue.dequeue();
            var successfulUpdate = this.workUpdate(update);
            if (!successfulUpdate) {
                // we should stop the iteration here
                break;
            }
        }
    }

    private void completeIfLoaded(int x, int z, ChunkAndClaim chunkAndClaim) {
        var index = CoordConversion.chunkIndex(x, z);
        var chunk = this.chunks.get(index);
        if (chunk == null) return;
        this.complete(chunkAndClaim.chunkFuture(), chunk);
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
    private boolean workUpdate(PrioritizedUpdate update) {
        var x = update.x();
        var z = update.z();
        // check if update priority changed. This could be because a high priority claim was removed.
        var chunkIndex = CoordConversion.chunkIndex(x, z);
        var entry = this.findHighestPriorityEntry(x, z);
        if (entry == null) {
            // last claim was removed. We may have to remove the chunk from the chunks map, if that hasn't happened already
            var chunk = this.chunks.remove(chunkIndex);
            if (chunk != null) {
                this.unloadChunk(chunk);
                // we have to propagate the updates to (potentially) unload neighbours
                this.propagateUpdates(null, update.priority, x, z, UpdateType.UNLOAD_PROPAGATE);
            }
            assert !this.chunks.containsKey(chunkIndex);
            return true;
        }

        var highestClaimPriority = calculatePriority(entry, x, z);
        if (update.priority() > highestClaimPriority + Vec.EPSILON) {
            // higher priority claim was removed, lower priority claim remaining for chunk.
            // Ignore this stale update caused by higher priority.
            return true;
        }
        if (update.priority() < highestClaimPriority - Vec.EPSILON) {
            // update from lower priority claim.
            // this should already have been handled by the update for the higher priority claim, so
            // we can just go next.
            return true;
        }


        var chunk = this.chunks.get(chunkIndex);
        if (chunk != null) {
            // if the chunk is already up-to-date we can also ignore this update.
            // This could especially be a problem if manifold priority drop is used, because
            // updates with the same existing priority will be scheduled and propagated down the line.
            // This can be prevented by saving the last update priority of a chunk
            var lastUpdatePriority = this.chunksLastUpdatePriority.get(chunkIndex);
            if (highestClaimPriority > lastUpdatePriority - Vec.EPSILON && highestClaimPriority < lastUpdatePriority + Vec.EPSILON) {
                // same priority (compared with Vec.EPSILON precision)
                // The update has already been handled for this chunk, we can ignore it
                return true;
            }
            this.chunksLastUpdatePriority.put(chunkIndex, highestClaimPriority);
        } else {
            // same as above with loadedChunk
            var loadingChunk = this.loadingChunks.get(chunkIndex);
            if (loadingChunk != null) {
                if (highestClaimPriority > loadingChunk.lastUpdatePriority - Vec.EPSILON && highestClaimPriority < loadingChunk.lastUpdatePriority + Vec.EPSILON) {
                    // same priority (compared with Vec.EPSILON precision)
                    // The update has already been handled for this chunk, we can ignore it
                    return true;
                }
                loadingChunk.lastUpdatePriority = highestClaimPriority;
            }
        }

        // we have to propagate the updates to (potentially) load neighbours
        this.propagateUpdates(entry, highestClaimPriority, x, z, UpdateType.LOAD_PROPAGATE);

        // The chunk may already be loaded, only start a worker for unloaded chunks
        if (chunk == null) {

            // if we are already loading the chunk, we can just use that task
            var loading = this.loadingChunks.get(chunkIndex);
            if (loading != null) {
                return true;
            }

            // if we are in the process of unloading/saving the chunk, we can use the cached chunk
            var unloading = this.unloadingChunks.get(chunkIndex);
            if (unloading != null) {
                var task = new LoadTask(x, z, highestClaimPriority);
                this.loadingChunks.put(chunkIndex, task);
                return tryRunOnWorker(() -> {
                    try {
                        var copy = unloading.copy(unloading.getInstance(), x, z);
                        this.workerFinishedGeneration(copy);
                    } catch (Throwable throwable) {
                        LOGGER.error("Exception while re-loading chunk", throwable);
                    }
                });
            }

            var task = new LoadTask(x, z, highestClaimPriority);
            this.loadingChunks.put(chunkIndex, task);

            return tryRunOnWorker(() -> {
                try {
                    this.workerGenerateChunk(task);
                } catch (Throwable throwable) {
                    LOGGER.error("Exception during chunk loading/generation", throwable);
                }
            });
        }

        return true;
    }

    private static boolean tryRunOnWorker(Runnable runnable) {
        // try to reserve a worker
        if (!AVAILABLE_TASKS.tryAcquire()) {
            // no worker available
            return false;
        }
        runOnWorker(() -> {
            try {
                runnable.run();
            } catch (Throwable t) {
                LOGGER.error("Exception during task on worker", t);
            } finally {
                AVAILABLE_TASKS.release();
            }
        });
        return true;
    }

    /**
     * Wrapper for worker execution.
     * The worker logic could be improved to dynamically change its behaviour based on detected uses.
     * If the average chunk loads are very fast (void gen/simple gen) we could use virtual threads with a lower
     * permit count (as not to use all carrier threads).
     * Even the carrier threads used by chunk generation will not be used very long and the virtual thread FIFO task queue
     * will not fill up because of the way loads are lazy.
     */
    private static void runOnWorker(Runnable runnable) {
        WORKER_EXECUTOR.execute(runnable);
    }

    private void unloadChunk(@NotNull Chunk chunk) {

    }

    private void workerGenerateChunk(LoadTask task) {
        final var loader = this.chunkLoader;
        final var supplier = this.chunkSupplier;
        final var generator = this.generator;
        if (!loader.supportsParallelLoading()) {
            // TODO maybe revisit and add locking to allow for non-parallel loaders, but not right now
            throw new AssertionError("ChunkLoaders must support parallel loading. Please migrate your system");
        }

        var x = task.x;
        var z = task.z;

        var chunk = loader.loadChunk(instance, x, z);
        if (chunk == null) {
            // Loader couldn't load the chunk, generate it
            chunk = chunkGenerationHandler.createChunk(supplier, generator, x, z);
            chunk.onGenerate();
        }

        this.workerFinishedGeneration(chunk);
    }

    private void workerFinishedGeneration(Chunk chunk) {
        this.chunkAccess.onLoad(chunk);
        this.addTask(new Task.ChunkGenerationFinished(chunk));
        // TODO
    }

    private double calculatePriority(@NotNull ChunkClaimTree.CompleteEntry entry, int x, int z) {
        return entry.entry().priority() - PRIORITY_DROP.calculate(entry.centerX(), entry.centerZ(), x, z);
    }

    private @Nullable ChunkClaimTree.CompleteEntry findHighestPriorityEntry(int x, int z) {
        var entries = this.tree.findEntries(x, z);
        if (entries.isEmpty()) return null;
        var comparator = entryComparator(x, z);
        return entries.stream().max(comparator).orElseThrow();
    }

    private static Comparator<ChunkClaimTree.CompleteEntry> entryComparator(int x, int z) {
        return Comparator.comparingDouble(e -> {
            var drop = PRIORITY_DROP.calculate(e.centerX(), e.centerZ(), x, z);
            return e.entry().priority() - drop;
        });
    }

    private void signal() {
        // we can check if the signalled flag is already set.
        // If that is true, then the one full iteration will start, but has not started yet, so we don't have to lock
        // All submitted data will be handled in the next iteration at the latest.
        // avoiding locking when signalling (when possible) has benefits, because the worker will be able to read with 
        // less contention, and all operations should just happen faster.
        if (this.signalled) return;
        this.signalLock.lock();
        try {
            // re-check signalled, maybe someone else already signalled?
            if (this.signalled) return;
            this.signalled = true;
            // signal. We have only 1 reader (manager thread), so we don't need signalAll()
            this.hasSignalled.signal();
        } finally {
            this.signalLock.unlock();
        }
    }

    private void addTask(Task task) {
        this.tasks.add(task);
        this.signal();
    }

    /**
     * TODO
     * Handle future completion on another thread, so we can be sure nobody messes with this thread.
     * One problem with this is order... Starting a virtual thread per future removes any guarantees about the order of updates...
     * One possible approach would be an update queue with a lock and start a virtual thread that does all required queue work.
     */
    private <T> void complete(CompletableFuture<T> future, T value) {
        future.complete(value);
//        Thread.startVirtualThread(() -> future.complete(value));
    }

    /**
     * TODO
     * Handle future completion on another thread, so we can be sure nobody messes with this thread.
     * One problem with this is order... Starting a virtual thread per future removes any guarantees about the order of updates...
     * One possible approach would be an update queue with a lock and start a virtual thread that does all required queue work.
     */
    private void completeExceptionally(CompletableFuture<?> future, Throwable throwable) {
        future.completeExceptionally(throwable);
//        Thread.startVirtualThread(() -> future.completeExceptionally(throwable));
    }

    public void addClaim(int x, int z, ChunkAndClaim chunkAndClaim) {
        addTask(new Task.AddClaim(x, z, chunkAndClaim));
    }

    public void removeClaim(ChunkClaim claim, @NotNull CompletableFuture<Void> future) {
        addTask(new Task.RemoveClaim(claim, future));
    }

    public @UnmodifiableView @NotNull Collection<@NotNull Chunk> getLoadedChunks() {
        return Collections.unmodifiableCollection(chunks.values());
    }

    /**
     * {@link PrioritizedUpdate#updateType} should only be used for prioritizing, not to execute different update functionality
     */
    private record PrioritizedUpdate(UpdateType updateType, double priority, int x, int z) {
        /**
         * This comparator first compares by update type, then by priority.
         * Update type order is:
         * - {@link UpdateType#REMOVE_CLAIM_EXPLICIT}
         * - {@link UpdateType#ADD_CLAIM_EXPLICIT}
         * - {@link UpdateType#UNLOAD_PROPAGATE}
         * - {@link UpdateType#LOAD_PROPAGATE}
         * <p>
         * In a specific update type, the order is descending by priority.
         */
        private static final Comparator<PrioritizedUpdate> COMPARATOR = Comparator.comparing(PrioritizedUpdate::updateType).thenComparingDouble(PrioritizedUpdate::priority).reversed();
    }

    /**
     * <b>Order of the entries is important. Ordered from least important to most important.</b>
     */
    private enum UpdateType {
        /**
         * When a normal load update happens, which was not explicitly requested (origin of claim).
         * Basically when a claim tries to load a chunk because of the radius of the claim.
         */
        LOAD_PROPAGATE(false),
        /**
         * When an unload update occurs. This should always be handled before any implicit loads,
         * to make sure we actually unload chunks at some point. Otherwise, if there are many chunks
         * to load and the unload update has a low priority, it will never get handled.
         */
        UNLOAD_PROPAGATE(false),
        /**
         * Used for the origin chunk of a claim. All origin chunks are handled as prioritized and handled
         * before any implicit updates. This should make the entire system more snappy to requests.
         */
        ADD_CLAIM_EXPLICIT(true),
        /**
         * Used for the origin chunk of a claim. All origin chunks are handled as prioritized and handled
         * before any implicit updates. This should make the entire system more snappy to requests.
         */
        REMOVE_CLAIM_EXPLICIT(true);

        private final boolean explicit;

        UpdateType(boolean explicit) {
            this.explicit = explicit;
        }

        public boolean isExplicit() {
            return explicit;
        }
    }

    private record ClaimedChunk(int x, int z, CompletableFuture<Chunk> future) {
    }

    private static final class LoadTask {
        private final int x;
        private final int z;
        private double lastUpdatePriority;

        private LoadTask(int x, int z, double lastUpdatePriority) {
            this.x = x;
            this.z = z;
            this.lastUpdatePriority = lastUpdatePriority;
        }
    }

    private sealed interface Task {
        record AddClaim(int x, int z, ChunkAndClaim chunkAndClaim) implements Task {
        }

        record RemoveClaim(@NotNull ChunkClaim claim, @NotNull CompletableFuture<Void> future) implements Task {
        }

        record ChunkGenerationFinished(@NotNull Chunk chunk) implements Task {
        }
    }

    /**
     * Used to calculate how much the priority should drop for a chunk with a given distance from the center of the claim.
     * This is used to make closer chunks load first.
     */
    private sealed interface PriorityDrop {
        /**
         * Calculates the drop-off for the chunk at x,z.
         * Returns a double, this should only be used internally.
         */
        double calculate(int claimX, int claimZ, int x, int z);

        /**
         * deltaX^2 + deltaZ^2
         */
        record HypotenuseSquared() implements PriorityDrop {
            @Override
            public double calculate(int claimX, int claimZ, int x, int z) {
                var dx = claimX - x;
                var dz = claimZ - z;
                return dx * dx + dz * dz;
            }
        }

        /**
         * sqrt(deltaX^2 + deltaZ^2)
         */
        record Hypotenuse() implements PriorityDrop {
            @Override
            public double calculate(int claimX, int claimZ, int x, int z) {
                var dx = claimX - x;
                var dz = claimZ - z;
                return Math.sqrt(dx * dx + dz * dz);
            }
        }

        /**
         * deltaX + deltaZ
         */
        record Simple() implements PriorityDrop {
            @Override
            public double calculate(int claimX, int claimZ, int x, int z) {
                var dx = claimX - x;
                var dz = claimZ - z;
                return Math.abs(dx) + Math.abs(dz);
            }
        }
    }

    /**
     * friendly shutdown without using interrupts or similar.
     *
     * @return the future for when shutdown has completed
     */
    public CompletableFuture<?> shutdown() {
        this.exit = true;
        this.signal();
        this.WORKER_EXECUTOR.shutdownNow();
        return this.shutdownFuture.thenCompose(v -> {
            var fut = new CompletableFuture<Void>();
            // check this late, in the shutdownFuture. Maybe we don't even have to wait for termination
            if (this.WORKER_EXECUTOR.isTerminated()) {
                fut.complete(null);
            } else {
                Thread.startVirtualThread(() -> {
                    try {
                        if (!this.WORKER_EXECUTOR.awaitTermination(5, TimeUnit.SECONDS)) {
                            LOGGER.error("Generator pool termination took more than 5 seconds. This indicates the generation for a single chunk took more than 5 seconds. No bueno");
                        }
                    } catch (InterruptedException e) {
                        LOGGER.error("Interrupted generation pool termination waiting. This should not happen", e);
                    } finally {
                        fut.complete(null);
                    }
                });
            }
            return fut;
        });
    }

    static {
        WORKER_EXECUTOR = new ForkJoinPool(Runtime.getRuntime().availableProcessors(), pool -> new ForkJoinWorkerThread(pool) {
            {
                // set the priority very low. Generation takes time and resources, we don't want generation to slow down any
                // other logic, such as ticking. Low priority does not mean less total CPU gets used, it just tells the scheduler
                // to prefer more important tasks, and if there is nothing important to do, then do generation.
                setPriority(Thread.MIN_PRIORITY);
            }
        }, null, true);
    }
}
