package net.minestom.server.instance.chunksystem;

import net.minestom.server.instance.Chunk;
import org.jetbrains.annotations.NotNull;

/**
 * Callbacks that will be called for a {@link ChunkClaim}.
 * <p>
 * All callbacks have no threading guarantees. They may be called from any thread.
 * Users must make sure to synchronize properly.
 */
public interface ClaimCallbacks {
    /**
     * Called once when all chunks required by a {@code claim} have been loaded.
     * <p>
     * Guaranteed to be called AFTER the last {@link #chunkLoaded(ChunkClaim, Chunk)} invocation has completed.
     * This will never be called if the claim is removed before all chunks have been loaded.
     * <p>
     * Because of the async nature of the Chunk System, if all chunks have been loaded, and the claim is removed,
     * this can be called even after {@link ChunkManager#removeClaim(ChunkClaim)}.
     * Users must make sure to check for this.
     *
     * @param claim the claim whose chunks are loaded.
     */
    default void allChunksLoaded(@NotNull ChunkClaim claim) {
    }

    /**
     * Called for every (loaded) chunk contained by this claim.
     * <p>
     * Because of the async nature of the Chunk System, if the claim is removed,
     * this can be called even after {@link ChunkManager#removeClaim(ChunkClaim)}.
     * Users must make sure to check for this.
     *
     * @param claim the claim whose chunk is loaded
     * @param chunk the chunk being loaded
     * @implNote It should be noted that in the current implementation it is not
     * guaranteed that {@link ChunkManager#getLoadedChunk(int, int)} returns a chunk with
     * the same chunk position. This can be because, the chunk is already unloaded again,
     * or because it is not yet added to the HashMap used for {@link ChunkManager#getLoadedChunk(int, int)}.
     * This may seem very weird, but it makes sense to have a separate HashMap for
     * {@link ChunkManager#getLoadedChunk(int, int)}, which is only updated at the beginning
     * of ticks. Otherwise, a chunk could be unloaded mid-tick-logic, which is arguably undesirable.
     * <p>
     * <b>TL;DR</b> If you need the {@link Chunk} instance, always use the passed instance, never
     * use {@link ChunkManager#getLoadedChunk(int, int)}. This also applies when you run delayed logic.
     */
    default void chunkLoaded(@NotNull ChunkClaim claim, @NotNull Chunk chunk) {
    }
}
