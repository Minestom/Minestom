package net.minestom.server.instance.chunksystem;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectHeapPriorityQueue;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.instance.InstanceChunkLoadEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.utils.chunk.ChunkSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.vectrix.flare.fastutil.Long2ObjectSyncMap;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

import static net.minestom.server.instance.chunksystem.ChunkClaimManager.*;

class SingleThreadedManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(SingleThreadedManager.class);
    private final ChunkClaimTree tree = new ChunkClaimTree();
    private final ChunkClaimManager chunkClaimManager;
    private final Long2DoubleMap chunksLastUpdatePriority = new Long2DoubleOpenHashMap();
    private final Long2ObjectMap<ChunkClaimManager.LoadTask> loadingChunks = new Long2ObjectOpenHashMap<>();
    /**
     * This HashMap is used to quickly access loaded chunks in case of added claims after they are scheduled for unload.
     * Otherwise, we'd have to wait for the unload/write to finish, and only then could we start the next load.
     * With this, we cache the chunk, and if it is loaded again, we can just copy it and use the new copy.
     * This way we also ensure the chunks are not changed while saving, which may cause issues with the ChunkLoader/-Saver.
     */
    private final Long2ObjectMap<ChunkClaimManager.UnloadTask> unloadingChunks = new Long2ObjectOpenHashMap<>();
    /**
     * HashMap for saving chunks. We don't want to save the same chunk concurrently, and we don't want unnecessary saves
     */
    private final Long2ObjectMap<ChunkClaimManager.SaveTask> savingChunks = new Long2ObjectOpenHashMap<>();
    /**
     * HashMap to contain all chunks that have their save request delayed. A chunk may be requested for saving
     * while it is being saved already. This save will work on old data though, we want the newest data, so we have
     * to reschedule the save after the first save finishes.
     */
    private final Long2ObjectMap<ChunkClaimManager.SaveTask> savingChunksDelayed = new Long2ObjectOpenHashMap<>();
    /**
     * Identity strategy, so we can identify the correct claims and remove them.
     */
    private final Object2ObjectMap<ChunkClaim, ChunkClaimManager.ClaimedChunk> claimMap = new Object2ObjectOpenHashMap<>();
    private final Long2ObjectMap<Collection<ChunkClaimManager.ClaimedChunk>> claimsByChunk = new Long2ObjectOpenHashMap<>();
    private final ObjectHeapPriorityQueue<PrioritizedUpdate> updateQueue = new ObjectHeapPriorityQueue<>(PrioritizedUpdate.COMPARATOR);
    private final ChunkWorker chunkWorker;
    private final Instance instance;
    /**
     * (chunk index -> chunk) map, contains all the chunks in the instance.
     * This is the only object int this class which is thread-safe for read only.
     */
    final Long2ObjectSyncMap<Chunk> chunks = Long2ObjectSyncMap.hashmap();
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

    public SingleThreadedManager(ChunkClaimManager chunkClaimManager, ChunkAccess chunkAccess) {
        this.chunkClaimManager = chunkClaimManager;
        this.chunkWorker = new ChunkWorker(chunkClaimManager, chunkAccess);
        this.instance = chunkClaimManager.getInstance();
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
                // we must resubmit the update to the queue
                this.updateQueue.enqueue(update);
                break;
            }
        }
    }

    void workAddClaim(int x, int z, ChunkAndClaim chunkAndClaim) {
        var claim = chunkAndClaim.chunkClaim();
        this.tree.insert(x, z, claim.radius(), claim.priority(), claim.shape());

        var claimedChunk = new ChunkClaimManager.ClaimedChunk(x, z, chunkAndClaim.chunkFuture());
        var chunkIndex = CoordConversion.chunkIndex(x, z);
        this.claimMap.put(claim, claimedChunk);
        this.claimsByChunk.computeIfAbsent(chunkIndex, c -> new HashSet<>(4)).add(claimedChunk);

        // If the chunk is already loaded, we can complete the future right here.
        // The new claim has been inserted into the map, the chunk may not be unloaded
        // as long as the claim exists, so there shouldn't be any issues with this.
        this.completeIfLoaded(x, z, chunkAndClaim);
        this.submitUpdate(x, z, claim.priority(), UpdateType.ADD_CLAIM_EXPLICIT);
    }

    void workRemoveClaim(ChunkClaim claim, CompletableFuture<Void> future) {
        var claimedChunk = this.claimMap.remove(claim);
        if (claimedChunk == null) {
            this.chunkClaimManager.completeExceptionally(future, new IllegalStateException("The claim you attempted to remove is not valid"));
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
        this.chunkClaimManager.complete(future, null);
        this.chunkClaimManager.completeExceptionally(claimedChunk.future(), new CancellationException("Claim was removed"));
    }

    void workChunkGenerationFinished(Chunk chunk) {
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

    void workSaveChunk(Chunk chunk, CompletableFuture<Void> future) {
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
            this.savingChunksDelayed.put(chunkIndex, new ChunkClaimManager.SaveTask(chunk, future));
            return;
        }
        this.workSaveChunk0(new ChunkClaimManager.SaveTask(chunk, future));
    }

    void workSaveChunk0(ChunkClaimManager.SaveTask saveTask) {
        // for saving with file IO we use a virtual thread instead of scheduling it to the worker.
        // This way we don't need to reserve a worker, which may otherwise prove to be difficult.
        var chunk = saveTask.chunk();
        var chunkIndex = CoordConversion.chunkIndex(chunk.getChunkX(), chunk.getChunkZ());
        var chunkLoader = this.chunkLoader;
        this.savingChunks.put(chunkIndex, saveTask);
        Thread.startVirtualThread(() -> {
            try {
                chunkLoader.saveChunk(chunk);
                this.chunkClaimManager.complete(saveTask.future(), null);
            } catch (Throwable t) {
                this.chunkClaimManager.completeExceptionally(saveTask.future(), t);
            } finally {
                this.chunkClaimManager.addTask(new ChunkClaimManager.Task.SaveChunkCompleted(chunk.getChunkX(), chunk.getChunkZ()));
            }
        });
    }

    void workSaveChunkCompleted(int x, int z) {
        var chunkIndex = CoordConversion.chunkIndex(x, z);
        if (this.savingChunksDelayed.containsKey(chunkIndex)) {
            var delayedTask = this.savingChunksDelayed.remove(chunkIndex);
            workSaveChunk0(delayedTask);
            return;
        }
        this.savingChunks.remove(chunkIndex);
    }

    void workSaveInstanceData(CompletableFuture<Void> future) {
        if (this.savingInstance) {
            if (this.savingInstanceDelayed != null) {
                link(savingInstanceDelayed, future);
                return;
            }
            this.savingInstanceDelayed = future;
            return;
        }
        this.workSaveInstanceData0(future);
    }

    void workSaveInstanceData0(CompletableFuture<Void> future) {
        // for saving with file IO we use a virtual thread instead of scheduling it to the worker.
        // This way we don't need to reserve a worker, which may otherwise prove to be difficult.
        this.savingInstance = true;
        var chunkLoader = this.chunkLoader;
        Thread.startVirtualThread(() -> {
            try {
                chunkLoader.saveInstance(instance);
                this.chunkClaimManager.complete(future, null);
            } catch (Throwable t) {
                this.chunkClaimManager.completeExceptionally(future, t);
            } finally {
                this.chunkClaimManager.addTask(new ChunkClaimManager.Task.SaveInstanceDataCompleted());
            }
        });
    }

    void workSaveInstanceCompleted() {
        if (this.savingInstanceDelayed != null) {
            workSaveInstanceData0(this.savingInstanceDelayed);
            this.savingInstanceDelayed = null;
            return;
        }
        this.savingInstance = false;
    }

    void workSaveChunks(CompletableFuture<Void> future) {
        var futures = new CompletableFuture[this.chunks.size()];
        var i = 0;
        for (var chunk : this.chunks.values()) {
            var f = new CompletableFuture<Void>();
            futures[i++] = f;
            this.workSaveChunk(chunk, f);
        }
        link(CompletableFuture.allOf(futures), future);
    }

    void workSaveInstanceDataAndChunks(CompletableFuture<Void> future) {
        var chunks = new CompletableFuture<Void>();
        this.workSaveChunks(chunks);
        var data = new CompletableFuture<Void>();
        this.workSaveInstanceData(data);
        link(CompletableFuture.allOf(chunks, data), future);
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
            return this.updateNoRemainingClaims(chunkIndex, x, z, update);
        }

        var highestClaimPriority = calculatePriority(entry, x, z);

        // Check if the update is still valid. If not, we can ignore it
        if (!this.isValidUpdate(update.priority(), highestClaimPriority)) {
            return true;
        }

        var chunk = this.chunks.get(chunkIndex);

        // If the chunk is already up to date, we can also ignore this update.
        // This could especially be a problem if a manifold priority drop is used, because
        // updates with the same existing priority will be scheduled and propagated down the line.
        // This can be prevented by saving the last update priority of a chunk
        if (chunk != null) {
            if (this.checkUpToDateLoaded(chunkIndex, highestClaimPriority)) {
                return true;
            }
            // we have to propagate the updates to (potentially) load neighbors
            this.propagateUpdates(entry, highestClaimPriority, x, z, UpdateType.LOAD_PROPAGATE);
            return true;
        } else {
            // We may have to load the chunk. If we don't have a worker available, we can stop
            // this update here and reschedule it.
            if (!ChunkWorker.tryReserve()) return false;

            if (this.checkUpToDateLoading(chunkIndex, highestClaimPriority)) {
                return true;
            }

            // we have to propagate the updates to (potentially) load neighbors
            this.propagateUpdates(entry, highestClaimPriority, x, z, UpdateType.LOAD_PROPAGATE);

            // The chunk may already be loaded, only start a worker for unloaded chunks
            return this.updateLoadChunk(chunkIndex, x, z, highestClaimPriority);
        }
    }

    /**
     * This update must have a worker reserved before this method is called
     */
    private boolean updateLoadChunk(long chunkIndex, int x, int z, double highestClaimPriority) {
        // if we are already loading the chunk, we can just use that task
        var loading = this.loadingChunks.get(chunkIndex);
        if (loading != null) {
            // We didn't need the permit
            // TODO check if there is a good way to not have to reserve the permit, even if we don't end up needing it
            ChunkWorker.release();
            return true;
        }

        // if we are in the process of unloading/saving the chunk, we can use the cached chunk
        var unloadTask = this.unloadingChunks.get(chunkIndex);
        if (unloadTask != null) {
            return this.updateLoadChunkFromMemory(chunkIndex, x, z, highestClaimPriority, unloadTask);
        }

        var task = new ChunkClaimManager.LoadTask(x, z, highestClaimPriority);
        this.loadingChunks.put(chunkIndex, task);

        ChunkWorker.submitReserved(() -> {
            try {
                this.chunkWorker.workerGenerateChunk(task);
            } catch (Throwable throwable) {
                LOGGER.error("Exception during chunk loading/generation", throwable);
            }
        });
        return true;
    }

    /**
     * This update must have a worker reserved before this method is called
     */
    private boolean updateLoadChunkFromMemory(long chunkIndex, int x, int z, double highestClaimPriority, ChunkClaimManager.UnloadTask unloadTask) {
        // If the partition is not yet deleted, we must wait until it has finished.
        // Otherwise InstanceChunkLoadEvent/InstanceChunkUnloadEvent may be fired out of order for
        // the same chunk.
        // Example:
        // InstanceChunkLoadEvent - InstanceChunkLoadEvent - InstanceChunkUnloadEvent - InstanceChunkUnloadEvent
        var unloading = unloadTask.chunk;
        var task = new ChunkClaimManager.LoadTask(x, z, highestClaimPriority);
        this.loadingChunks.put(chunkIndex, task);
        if (unloadTask.partitionDeleted.isDone()) {
            ChunkWorker.submitReserved(() -> this.chunkWorker.workerCopyFromMemory(unloading, x, z));
            this.chunkClaimManager.addTask(new ChunkClaimManager.Task.CompleteLoadTask(x, z));
            return false;
        } else {
            unloadTask.partitionDeleted.whenComplete((unused, throwable) -> {
                if (throwable != null) {
                    LOGGER.error("Error in partitionDeleted future", throwable);
                }
                this.chunkClaimManager.addTask(new ChunkClaimManager.Task.CompleteLoadTask(x, z));
            });
            return true;
        }
    }

    private boolean updateNoRemainingClaims(long chunkIndex, int x, int z, PrioritizedUpdate update) {
        // The last claim was removed. We may have to remove the chunk from the chunks map if that hasn't happened already
        var chunk = this.chunks.remove(chunkIndex);
        if (chunk != null) {
            this.unloadChunk(chunk);
            // we have to propagate the updates to (potentially) unload neighbors
            this.propagateUpdates(null, update.priority(), x, z, UpdateType.UNLOAD_PROPAGATE);
        }
        return true;
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
        this.unloadingChunks.put(chunkIndex, new ChunkClaimManager.UnloadTask(chunk, unloadFuture));
        var scheduler = chunk.getScheduler();
        scheduler.scheduleNextProcess(() -> {
            MinecraftServer.process().dispatcher().deletePartition(chunk);
        });
        if (this.autosaveEnabled) {
            var saveFuture = new CompletableFuture<Void>();
            this.workSaveChunk(chunk, saveFuture);
            saveFuture.whenComplete((unused, throwable) -> {
                if (throwable != null) {
                    LOGGER.error("Exception when saving chunk", throwable);
                    return;
                }
                // TODO
            });
            // TODO
            return;
        }
    }


    /**
     * Finish unloading after saving has completed
     */
    void finishUnloadChunk(@NotNull Chunk chunk) {

    }

    void completeIfLoaded(int x, int z, ChunkAndClaim chunkAndClaim) {
        var index = CoordConversion.chunkIndex(x, z);
        var chunk = this.chunks.get(index);
        if (chunk == null) return;
        this.chunkClaimManager.complete(chunkAndClaim.chunkFuture(), chunk);
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
        if (highestClaimPriority > lastUpdatePriority - Vec.EPSILON && highestClaimPriority < lastUpdatePriority + Vec.EPSILON) {
            // same priority (compared with Vec.EPSILON precision)
            // The update has already been handled for this chunk, we can ignore it
            return true;
        }
        this.chunksLastUpdatePriority.put(chunkIndex, highestClaimPriority);
        return false;
    }

    private boolean checkUpToDateLoading(long chunkIndex, double highestClaimPriority) {
        var loadingChunk = this.loadingChunks.get(chunkIndex);
        if (loadingChunk != null) {
            if (highestClaimPriority > loadingChunk.lastUpdatePriority - Vec.EPSILON && highestClaimPriority < loadingChunk.lastUpdatePriority + Vec.EPSILON) {
                // same priority (compared with Vec.EPSILON precision)
                // The update has already been handled for this chunk, we can ignore it
                return true;
            }
            loadingChunk.lastUpdatePriority = highestClaimPriority;
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

    /**
     * Calculates the priority of a given {@code entry} for a chunk at {@code x,z}
     */
    static double calculatePriority(@NotNull ChunkClaimTree.CompleteEntry entry, int x, int z) {
        return entry.entry().priority() - PRIORITY_DROP.calculate(entry.centerX(), entry.centerZ(), x, z);
    }

    static Comparator<ChunkClaimTree.CompleteEntry> entryComparator(int x, int z) {
        return Comparator.comparingDouble(e -> calculatePriority(e, x, z));
    }
}
