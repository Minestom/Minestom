package net.minestom.server.instance.chunksystem.impl;

import it.unimi.dsi.fastutil.Function;
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
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.instance.InstanceChunkLoadEvent;
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
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

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
    private final Object2ObjectMap<ChunkClaim, ClaimedChunk> claimMap = new Object2ObjectOpenCustomHashMap<>(IDENTITY);
    private final Long2ObjectMap<Collection<ClaimedChunk>> claimsByChunk = new Long2ObjectOpenHashMap<>();
    private final ObjectHeapPriorityQueue<PrioritizedUpdate> updateQueue = new ObjectHeapPriorityQueue<>(PrioritizedUpdate.COMPARATOR);
    private final Instance instance;
    private final ChunkGenerationHandler chunkGenerationHandler;
    private final ConcurrentLinkedQueue<Task> tasks = new ConcurrentLinkedQueue<>();
    private final ChunkAccess chunkAccess;
    private final ManagerSignaling signaling = new ManagerSignaling();
    private volatile boolean exit = false;

    /**
     * Same as {@link #savingChunks} for instance data
     */
    private boolean savingInstance = false;
    private CompletableFuture<Void> savingInstanceDelayed = null;
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
        // We finally start ourselves.
        // Now we start accepting tasks.
        // Virtual thread is appropriate here, we
        // will spend most time waiting for signals/workers.
        // Also, 1 platform thread per instance can become quite expensive
        Thread.startVirtualThread(this);
    }

    @Override
    public void run() {
        while (!this.exit) {
            this.signaling.startIteration();
            while (true) {
                var task = this.tasks.poll();
                if (task == null) break;
                this.workTask(task);
            }
            this.workIteration();
            if (this.hasUpdated || this.exit) continue;

            if (this.signaling.waitForSignal()) {
                // A problem occurred. Probably an InterruptedException
                break;
            }
        }
        // after we shut down normally, we shouldn't have to process the remaining tasks.
        // TODO check in the future if this assumption holds
    }

    public @NotNull IChunkLoader getChunkLoader() {
        return this.chunkLoader;
    }

    public void setChunkLoader(@Nullable IChunkLoader chunkLoader) {
        this.chunkLoader = Objects.requireNonNullElse(chunkLoader, IChunkLoader.noop());
    }

    public @NotNull ChunkSupplier getChunkSupplier() {
        return this.chunkSupplier;
    }

    public void setChunkSupplier(@NotNull ChunkSupplier chunkSupplier) {
        this.chunkSupplier = chunkSupplier;
    }

    public boolean isAutosaveEnabled() {
        return this.autosaveEnabled;
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
            case Task.SaveChunk saveChunk -> workSaveChunk(saveChunk.chunk, saveChunk.future);
            case Task.SaveChunks saveChunks -> workSaveChunks(saveChunks.future);
            case Task.SaveInstanceData saveInstanceData -> workSaveInstanceData(saveInstanceData.future);
            case Task.SaveInstanceDataAndChunks saveInstanceDataAndChunks ->
                    workSaveInstanceDataAndChunks(saveInstanceDataAndChunks.future);
        }
    }

    private void workAddClaim(int x, int z, ChunkAndClaim chunkAndClaim) {
        var claim = chunkAndClaim.chunkClaim();
        this.tree.insert(x, z, claim.radius(), claim.priority(), claim.shape());

        var claimedChunk = new ClaimedChunk(x, z, chunkAndClaim.chunkFuture());
        var chunkIndex = CoordConversion.chunkIndex(x, z);
        this.claimMap.put(claim, claimedChunk);
        this.claimsByChunk.computeIfAbsent(chunkIndex, c -> new HashSet<>(4)).add(claimedChunk);

        // If the chunk is already loaded, we can complete the future right here.
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
        // We can complete the future right here, the claim was removed.
        // Removing a claim makes no guarantees about when the chunk is unloaded, so this is the easiest
        // and most obvious place to complete the future
        this.complete(future, null);
        this.completeExceptionally(claimedChunk.future(), new CancellationException("Claim was removed"));
    }

    private void workChunkGenerationFinished(Chunk chunk) {
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
        chunk.getScheduler().scheduleNextProcess(() -> {
            var event = new InstanceChunkLoadEvent(this.instance, chunk);
            EventDispatcher.call(event);
        });
        MinecraftServer.process().dispatcher().createPartition(chunk);
    }

    private void workSaveChunk(Chunk chunk, CompletableFuture<Void> future) {
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
                link(saving.future, future);
                return;
            }
            // this is enough to schedule the chunk to be saved again
            this.savingChunksDelayed.put(chunkIndex, new SaveTask(chunk, future));
            return;
        }
        workSaveChunk0(new SaveTask(chunk, future));
    }

    private void workSaveChunk0(SaveTask saveTask) {
        var chunk = saveTask.chunk;
        var chunkIndex = CoordConversion.chunkIndex(chunk.getChunkX(), chunk.getChunkZ());
        this.savingChunks.put(chunkIndex, saveTask);
    }

    private void workSaveInstanceData(CompletableFuture<Void> future) {
        if (this.savingInstance) {
            if (this.savingInstanceDelayed != null) {
                link(savingInstanceDelayed, future);
                return;
            }
            this.savingInstanceDelayed = future;
            return;
        }
    }

    private void workSaveInstanceData0() {

    }

    private void workSaveChunks(CompletableFuture<Void> future) {
        var futures = new CompletableFuture[this.chunks.size()];
        var i = 0;
        for (var chunk : this.chunks.values()) {
            var f = new CompletableFuture<Void>();
            futures[i++] = f;
            this.workSaveChunk(chunk, f);
        }
        link(CompletableFuture.allOf(futures), future);
    }

    private void workSaveInstanceDataAndChunks(CompletableFuture<Void> future) {
        var chunks = new CompletableFuture<Void>();
        this.workSaveChunks(chunks);
        var data = new CompletableFuture<Void>();
        this.workSaveInstanceData(data);
        link(CompletableFuture.allOf(chunks, data), future);
    }

    private void uncacheChunk(Chunk chunk) {
        this.instance
                .getEntityTracker()
                .chunkEntities(chunk.getChunkX(), chunk.getChunkZ(), EntityTracker.Target.ENTITIES)
                .forEach(Entity::remove);
        var loadedChunk = this.chunks.remove(CoordConversion.chunkIndex(chunk.getChunkX(), chunk.getChunkZ()));
        // TODO
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
            var priority = this.calculatePriority(origin, x, z);
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

    /**
     * This method is responsible for selecting and submitting the chunks to load.
     */
    private void workIteration() {
        // All updates up to here have been seen. We will handle them now.
        this.hasUpdated = false;

        // This loop should not run very long. It will first do any maintenance work (removing stale updates)
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
        // Check if update priority changed. This could be because a high priority claim was removed.
        var chunkIndex = CoordConversion.chunkIndex(x, z);
        var entry = this.findHighestPriorityEntry(x, z);
        if (entry == null) {
            // The last claim was removed. We may have to remove the chunk from the chunks map if that hasn't happened already
            var chunk = this.chunks.remove(chunkIndex);
            if (chunk != null) {
                this.unloadChunk(chunk);
                // we have to propagate the updates to (potentially) unload neighbors
                this.propagateUpdates(null, update.priority(), x, z, UpdateType.UNLOAD_PROPAGATE);
            }
            return true;
        }

        var highestClaimPriority = calculatePriority(entry, x, z);

        // Check if the update is still valid. If not, we can ignore it
        if (!this.isValidUpdate(update.priority(), highestClaimPriority)) {
            return true;
        }

        var chunk = this.chunks.get(chunkIndex);

        if (chunk != null) {
            // If the chunk is already up to date, we can also ignore this update.
            // This could especially be a problem if a manifold priority drop is used, because
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

        // we have to propagate the updates to (potentially) load neighbors
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
                return ChunkWorker.tryRunOnWorker(() -> {
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

            return ChunkWorker.tryRunOnWorker(() -> {
                try {
                    this.workerGenerateChunk(task);
                } catch (Throwable throwable) {
                    LOGGER.error("Exception during chunk loading/generation", throwable);
                }
            });
        }

        return true;
    }

    private void unloadChunk(@NotNull Chunk chunk) {
        var x = chunk.getChunkX();
        var z = chunk.getChunkZ();
        var chunkIndex = CoordConversion.chunkIndex(x, z);
        unloadingChunks.put(chunkIndex, chunk);
        var scheduler = chunk.getScheduler();
        scheduler.scheduleNextProcess(() -> {

        });
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

    private void addTask(Task task) {
        this.tasks.add(task);
        this.signaling.signal();
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

    private static <T> void link(CompletableFuture<T> future1, CompletableFuture<T> future2) {
        link(future1, future2, v -> (T) v);
    }

    private static <T, V> void link(CompletableFuture<T> future1, CompletableFuture<V> future2, Function<T, V> function) {
        future1.whenComplete((val, throwable) -> {
            if (throwable != null) future2.completeExceptionally(throwable);
            else future2.complete(function.apply(val));
        });
    }

    public void addClaim(int x, int z, @NotNull ChunkAndClaim chunkAndClaim) {
        this.addTask(new Task.AddClaim(x, z, chunkAndClaim));
    }

    public void removeClaim(@NotNull ChunkClaim claim, @NotNull CompletableFuture<Void> future) {
        this.addTask(new Task.RemoveClaim(claim, future));
    }

    public void saveChunk(@NotNull Chunk chunk, CompletableFuture<Void> future) {
        this.addTask(new Task.SaveChunk(chunk, future));
    }

    public void saveChunks(@NotNull CompletableFuture<Void> future) {
        this.addTask(new Task.SaveChunks(future));
    }

    public void saveInstanceData(@NotNull CompletableFuture<Void> future) {
        this.addTask(new Task.SaveInstanceData(future));
    }

    public void saveInstanceDataAndChunks(@NotNull CompletableFuture<Void> future) {
        this.addTask(new Task.SaveInstanceDataAndChunks(future));
    }

    public @UnmodifiableView @NotNull Collection<@NotNull Chunk> getLoadedChunks() {
        return Collections.unmodifiableCollection(this.chunks.values());
    }

    /**
     * friendly shutdown without using interrupts or similar.
     *
     * @return the future for when shutdown has completed
     */
    public CompletableFuture<?> shutdown() {
        this.exit = true;
        this.signaling.signal();
        return this.shutdownFuture;
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

    private record SaveTask(@NotNull Chunk chunk, @NotNull CompletableFuture<Void> future) {
    }

    private sealed interface Task {
        record AddClaim(int x, int z, ChunkAndClaim chunkAndClaim) implements Task {
        }

        record RemoveClaim(@NotNull ChunkClaim claim, @NotNull CompletableFuture<Void> future) implements Task {
        }

        record SaveChunk(@NotNull Chunk chunk, @NotNull CompletableFuture<Void> future) implements Task {
        }

        record SaveChunks(@NotNull CompletableFuture<Void> future) implements Task {
        }

        record SaveInstanceData(@NotNull CompletableFuture<Void> future) implements Task {
        }

        record SaveInstanceDataAndChunks(@NotNull CompletableFuture<Void> future) implements Task {
        }

        record ChunkGenerationFinished(@NotNull Chunk chunk) implements Task {
        }
    }

    private record ClaimedChunk(int x, int z, CompletableFuture<Chunk> future) {
    }
}
