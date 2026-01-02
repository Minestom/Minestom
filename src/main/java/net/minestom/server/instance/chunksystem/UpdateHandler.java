package net.minestom.server.instance.chunksystem;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.chunksystem.SingleThreadedManager.UpdateResult;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static net.minestom.server.coordinate.CoordConversion.chunkIndex;
import static net.minestom.server.instance.chunksystem.SingleThreadedManager.callbacks;
import static net.minestom.server.instance.chunksystem.SingleThreadedManager.executeVirtual;

/**
 * Logic for update handling.
 * <p>
 * Separated from {@link SingleThreadedManager} for better understanding
 */
class UpdateHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateHandler.class);
    private final SingleThreadedManager singleThreadedManager;
    /**
     * Managed by loaders themselves and won't be interfered with.
     * This can differ from the state in the chunks HashMap, because the chunk can be unloaded while being loaded.
     */
    private final Long2ObjectMap<State.@Nullable Loading> loadingChunks = new Long2ObjectOpenHashMap<>();
    private final Long2ObjectMap<@Nullable State> chunks = new Long2ObjectOpenHashMap<>();
    private final Long2ObjectMap<@Nullable SaveState> savingChunks = new Long2ObjectOpenHashMap<>();
    // Cache for tree queries to reduce memory allocations
    private final ReusableList<ChunkClaimTree.CompleteEntry> claimEntryCache = new ReusableList<>();
    private boolean savingInstance;
    private @Nullable CompletableFuture<Void> saveInstanceDelayed;

    public UpdateHandler(SingleThreadedManager singleThreadedManager) {
        this.singleThreadedManager = singleThreadedManager;
    }

    UpdateResult workUpdate(PrioritizedUpdate update, boolean disablePropagation) {
        if (update.updateType().isLoad() && !this.singleThreadedManager.hasClaim(update.origin())) {
            // Claim of update no longer exists, update is invalid
            return UpdateResult.INVALID_UPDATE;
        }
        var x = update.x();
        var z = update.z();
        this.singleThreadedManager.tree.findEntries(this.claimEntryCache, x, z);
        if (this.claimEntryCache.isEmpty()) {
            return this.updateNoRemainingClaims(update, disablePropagation);
        }
        try {
            return this.updateWithClaims(update, disablePropagation, this.claimEntryCache);
        } finally {
            this.claimEntryCache.clear();
        }
    }

    private UpdateResult updateWithClaims(PrioritizedUpdate update, boolean disablePropagation, ReusableList<ChunkClaimTree.CompleteEntry> entries) {
        var x = update.x();
        var z = update.z();
        var chunkIndex = chunkIndex(x, z);

        if (update.updateType().isUnload()) {
            var claimData = this.singleThreadedManager.claimMap.get(update.origin());
            assert claimData == null;
            // This chunk has claims, so the update doesn't apply to us.
            // We still have to propagate to chunks further away, though.
            // TODO would be nice if we can find a nice optimization to reduce
            //  number of chunks affected by unload propagation. Might be very
            //  difficult though, to future me: think of all edge cases, there are quite a few
            this.singleThreadedManager.updateQueue.propagateUpdates(update, null, disablePropagation);
            return UpdateResult.INVALID_UPDATE;
        }

        var highestEntry = this.singleThreadedManager.tree.findHighestPriorityEntry(entries, this.singleThreadedManager.priorityDrop, x, z);
        var highestPriority = highestEntry.entry().priority();
        if (update.priority() - Vec.EPSILON > highestPriority) {
            // The Priority of the update is higher than the highest priority claim.
            // This update is stale (from an origin with higher priority) and can be ignored
            return UpdateResult.INVALID_UPDATE;
        }
        var claimData = this.singleThreadedManager.claimMap.get(update.origin());
        assert claimData != null;

        var currentState = this.chunks.get(chunkIndex);
        return switch (currentState) {
            case null -> {
                // We do not know the chunk. Default "load chunk" case.
                this.singleThreadedManager.updateQueue.propagateUpdates(update, claimData, disablePropagation);
                if (!this.loadingChunks.containsKey(chunkIndex)) {
                    yield this.updateFirstLoad(update, claimData);
                } else {
                    var loadingChunk = this.loadingChunks.get(chunkIndex);
                    assert loadingChunk != null;
                    if (loadingChunk.claimsRegisteredForCallback.add(update.origin())) {
                        claimData.startLoad();
                    }
                }
                yield UpdateResult.LOAD_SCHEDULED_EXISTING;
            }
            case State.Loading loading -> {
                // TODO can this case even happen?
                this.singleThreadedManager.updateQueue.propagateUpdates(update, claimData, disablePropagation);
                if (loading.claimsRegisteredForCallback.add(update.origin())) {
                    claimData.startLoad();
                }
                yield UpdateResult.LOAD_SCHEDULED_EXISTING;
            }
            case State.Loaded loaded -> {
                // Chunk already loaded, so we just add the claim and propagate updates for priority.
                this.singleThreadedManager.updateQueue.propagateUpdates(update, claimData, disablePropagation);
                callbackLoaded(claimData, loaded.chunk);
                yield UpdateResult.UPDATE_PROPAGATED_FOR_LOADED;
            }
            case State.Unloading unloading -> {
                // We are currently unloading the chunk, but now we want it back.
                // Here, as an assumed optimization, we copy the chunk from the one we still have in memory.
                // Should be faster than generating a new chunk
                this.singleThreadedManager.updateQueue.propagateUpdates(update, claimData, disablePropagation);
                yield updateLoadChunkFromMemory(update, claimData, unloading);
            }
        };
    }

    private void callbackLoaded(SingleThreadedManager.ClaimData claimData, Chunk chunk) {
        var claim = claimData.claim;
        var cb = claim.callbacks();
        if (cb != null) {
            // Increment counter by one to make sure #allChunksLoaded is always called after the last #chunkLoaded invocation
            claimData.startLoad();
            // Start a virtual thread for the callback, so it can't interfere with the
            // chunk manager thread (like blocking operations)
            executeVirtual(() -> {
                try {
                    cb.chunkLoaded(claim, chunk);
                } catch (Throwable t) {
                    LOGGER.error("Exception in #chunkLoaded callback", t);
                } finally {
                    claimData.finishLoad();
                }
            });
        }
    }

    private UpdateResult updateLoadChunkFromMemory(PrioritizedUpdate update, SingleThreadedManager.ClaimData claimData, State.Unloading unloading) {
        var x = update.x();
        var z = update.z();
        if (unloading.partitionDeleted.isDone()) {
            if (!ChunkWorker.tryReserve()) {
                return UpdateResult.WAITING_FOR_WORKER;
            }

            var chunk = unloading.chunk;

            this.startLoad(claimData, update.origin(), x, z);

            this.singleThreadedManager.startWorkerCopyFromMemory(chunk, x, z);
            return UpdateResult.LOAD_COPY_SCHEDULED;
        }

        return new UpdateResult.WaitingForFuture(unloading.partitionDeleted, true);
    }

    private UpdateResult updateFirstLoad(PrioritizedUpdate update, SingleThreadedManager.ClaimData claimData) {
        if (!ChunkWorker.tryReserve()) {
            // No worker available, delay update
            return UpdateResult.WAITING_FOR_WORKER;
        }

        var x = update.x();
        var z = update.z();

        this.startLoad(claimData, update.origin(), x, z);

        this.singleThreadedManager.startWorkerGenerateChunk(x, z);

        return UpdateResult.LOAD_SCHEDULED;
    }

    private void startLoad(SingleThreadedManager.ClaimData claimData, ChunkClaim claim, int x, int z) {
        var chunkIndex = chunkIndex(x, z);
        var loading = new State.Loading();
        if (loading.claimsRegisteredForCallback.add(claim)) {
            claimData.startLoad();
        }
        this.chunks.put(chunkIndex, loading);
        this.loadingChunks.put(chunkIndex, loading);
        if (callbacks != null) {
            callbacks.onLoadStarted(x, z);
        }
    }

    private UpdateResult updateNoRemainingClaims(PrioritizedUpdate update, boolean disablePropagation) {
        // Chunk is not supposed to be loaded.
        // Check if chunk is loaded, if so, then unload
        if (update.updateType().isLoad()) {
            // If we don't have any claims, then the original claim
            // responsible for the update doesn't exist anymore.
            // Just ignore this and don't propagate
            return UpdateResult.INVALID_UPDATE;
        }
        var x = update.x();
        var z = update.z();
        var chunkIndex = chunkIndex(x, z);
        var currentState = this.chunks.remove(chunkIndex);
        this.singleThreadedManager.updateQueue.propagateUpdates(update, null, disablePropagation);
        switch (currentState) {
            case State.Loaded loaded -> {
                this.unloadChunk(loaded.chunk, currentState);
                return UpdateResult.UNLOAD_SCHEDULED;
            }
            case State.Loading ignored -> {
                // Chunk is loading, if we don't want it, we do nothing.
                // Chunk is already removed from the HashMap, will be removed from
                // loadingChunks once fully removed
                return UpdateResult.UNLOAD_SCHEDULED;
            }
            case null, default -> {
                // Already unloaded. We only propagate updates
                return UpdateResult.INVALID_UPDATE;
            }
        }
    }

    private void unloadChunk(Chunk chunk, State currentState) {
        var x = chunk.getChunkX();
        var z = chunk.getChunkZ();
        var chunkIndex = chunkIndex(x, z);

        if (currentState instanceof State.Unloading) {
            // Already unloading, nothing to do
            return;
        }
        var unloading = new State.Unloading(chunk);
        this.chunks.put(chunkIndex, unloading);

        this.singleThreadedManager.startUnloadChunk(unloading);
    }

    void finishUnloadAfterSaveAndPartition(State.Unloading unloading) {
        var chunk = unloading.chunk;
        var x = chunk.getChunkX();
        var z = chunk.getChunkZ();
        var chunkIndex = chunkIndex(x, z);
        this.chunks.remove(chunkIndex, unloading);
    }

    void saveChunk(Chunk chunk, CompletableFuture<Void> saveFuture) {
        var x = chunk.getChunkX();
        var z = chunk.getChunkZ();
        var chunkIndex = chunkIndex(x, z);
        var saveTask = this.savingChunks.get(chunkIndex);
        if (saveTask != null) {
            // An existing save request for a chunk at the given location already exists.
            while (saveTask.runAfter != null) {
                saveTask = saveTask.runAfter;

                if (saveTask.chunk == chunk) {
                    TaskSchedulerThread.link(saveTask.future, saveFuture);
                    return;
                }
            }
            saveTask.runAfter = new SaveState(saveFuture, chunk);
            return;
        }
        saveChunk0(new SaveState(saveFuture, chunk));
    }

    private void saveChunk0(SaveState saveState) {
        // Saving uses a custom pool so it doesn't clog the worker pool
        var chunk = saveState.chunk;
        var x = chunk.getChunkX();
        var z = chunk.getChunkZ();
        var chunkIndex = chunkIndex(x, z);
        this.savingChunks.put(chunkIndex, saveState);
        this.singleThreadedManager.startSavingChunk(chunk, saveState);
        if (callbacks != null) {
            callbacks.onSaveStarted(chunk);
        }
    }

    void saveChunkCompleted(Chunk chunk) {
        var x = chunk.getChunkX();
        var z = chunk.getChunkZ();
        var chunkIndex = chunkIndex(x, z);
        var saveTask = this.savingChunks.remove(chunkIndex);
        assert saveTask != null;
        if (callbacks != null) {
            callbacks.onSaveComplete(chunk);
        }
        if (saveTask.runAfter != null) {
            this.saveChunk0(saveTask.runAfter);
        }
    }

    void saveAllChunks(CompletableFuture<Void> future) {
        var futures = new ArrayList<CompletableFuture<Void>>();
        for (var chunk : this.chunks.values()) {
            if (chunk instanceof State.Loaded loaded) {
                var f = new CompletableFuture<Void>();
                futures.add(f);
                this.saveChunk(loaded.chunk, f);
            }
        }
        TaskSchedulerThread.link(CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)), future);
    }

    void saveInstanceData(CompletableFuture<Void> future) {
        if (this.savingInstance) {
            if (this.saveInstanceDelayed != null) {
                TaskSchedulerThread.link(this.saveInstanceDelayed, future);
                return;
            }
            this.saveInstanceDelayed = future;
            return;
        }
        saveInstanceData0(future);
    }

    private void saveInstanceData0(CompletableFuture<Void> future) {
        this.savingInstance = true;
        this.saveInstanceDelayed = null;
        this.singleThreadedManager.startSaveInstance(future);
    }

    void saveInstanceCompleted() {
        this.savingInstance = false;
        if (this.saveInstanceDelayed != null) {
            this.saveInstanceData0(this.saveInstanceDelayed);
        }
    }

    boolean tryChangeToLoaded(Chunk chunk) {
        var x = chunk.getChunkX();
        var z = chunk.getChunkZ();
        var chunkIndex = chunkIndex(x, z);
        var loading = this.loadingChunks.remove(chunkIndex);
        assert loading != null;
        this.singleThreadedManager.tree.findEntries(this.claimEntryCache, x, z);
        if (this.claimEntryCache.isEmpty()) {
            // Chunk has no remaining claims, it should not be loaded.
            // This can happen if a claim is removed while a chunk is in generation.
            this.chunks.remove(chunkIndex, loading);
            if (callbacks != null) {
                callbacks.onLoadCancelled(x, z);
            }
            return false;
        }
        this.claimEntryCache.clear(); // Reset cache
        if (callbacks != null) {
            callbacks.onLoadCompleted(x, z);
        }
        var loaded = new State.Loaded(chunk);
        this.chunks.put(chunkIndex, loaded);
        this.singleThreadedManager.loadedChunksManaged.put(chunkIndex, chunk);
        // TODO chunks can block us... This is undesirable
        chunk.setLoaded();
        chunk.onLoadManaged();

        for (var claim : loading.claimsRegisteredForCallback) {
            if (this.singleThreadedManager.hasClaim(claim)) {
                var data = this.singleThreadedManager.claimMap.get(claim);
                callbackLoaded(data, chunk);
                data.finishLoad();
            }
        }
        return true;
    }

    @Nullable Chunk getLoaded(int x, int z) {
        var chunkIndex = CoordConversion.chunkIndex(x, z);
        var state = chunks.get(chunkIndex);
        if (state instanceof State.Loaded loaded) {
            return loaded.chunk;
        }
        return null;
    }

    List<Chunk> singleClaimCopyTo(UpdateHandler copyTarget, Instance copyInstance) {
        var chunks = new ArrayList<Chunk>();
        for (var state : this.chunks.values()) {
            if (!(state instanceof State.Loaded loaded)) continue;
            var originalChunk = loaded.chunk;
            var chunk = originalChunk.copy(copyInstance, originalChunk.getChunkX(), originalChunk.getChunkZ());
            assert chunk.getClass() == originalChunk.getClass() : "The chunk class " + chunk.getClass().getName()
                    + " does not specify a \"copy\" method that keeps the class. New Class: " + chunk.getClass().getName();
            assert !chunk.isLoaded() : "Copied chunk must not be loaded";
            chunks.add(chunk);
            copyTarget.chunks.put(CoordConversion.chunkIndex(chunk.getChunkX(), chunk.getChunkZ()), new State.Loaded(chunk));
        }
        return chunks;
    }

    static final class SaveState {
        final CompletableFuture<Void> future;
        final Chunk chunk;
        @Nullable SaveState runAfter;

        public SaveState(CompletableFuture<Void> future, Chunk chunk) {
            this.future = future;
            this.chunk = chunk;
        }
    }

    sealed abstract static class State {
        static final class Loading extends State {
            final Collection<ChunkClaim> claimsRegisteredForCallback = new HashSet<>();
        }

        static final class Loaded extends State {
            final Chunk chunk;

            public Loaded(Chunk chunk) {
                this.chunk = chunk;
            }
        }

        static final class Unloading extends State {
            final Chunk chunk;
            final CompletableFuture<Void> unloadFuture = new CompletableFuture<>();
            final CompletableFuture<Void> partitionDeleted = new CompletableFuture<>();

            public Unloading(Chunk chunk) {
                this.chunk = chunk;
            }
        }
    }
}
