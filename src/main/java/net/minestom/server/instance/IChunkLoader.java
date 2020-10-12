package net.minestom.server.instance;

import net.minestom.server.utils.chunk.ChunkCallback;

/**
 * Interface implemented to change the way chunks are loaded/saved.
 * See {@link MinestomBasicChunkLoader} for the default implementation used in {@link InstanceContainer}.
 */
public interface IChunkLoader {

    /**
     * Load a {@link Chunk}, all blocks should be set since the {@link ChunkGenerator} is not applied
     *
     * @param instance the {@link Instance} where the {@link Chunk} belong
     * @param chunkX   the chunk X
     * @param chunkZ   the chunk Z
     * @param callback the callback executed when the {@link Chunk} is done loading,
     *                 never called if the method returns false.
     * @return true if the chunk loaded successfully, false otherwise
     */
    boolean loadChunk(Instance instance, int chunkX, int chunkZ, ChunkCallback callback);

    /**
     * Save a {@link Chunk} with a callback for when it is done
     *
     * @param chunk    the {@link Chunk} to save
     * @param callback the callback executed when the {@link Chunk} is done saving,
     *                 should be called even if the saving failed (you can throw an exception)
     */
    void saveChunk(Chunk chunk, Runnable callback);

    /**
     * Does this {@link IChunkLoader} allow for multi-threaded saving of {@link Chunk}?
     *
     * @return true if the chunk loader supports parallel saving
     */
    default boolean supportsParallelSaving() {
        return false;
    }

    /**
     * Does this {@link IChunkLoader} allow for multi-threaded loading of {@link Chunk}?
     *
     * @return true if the chunk loader supports parallel loading
     */
    default boolean supportsParallelLoading() {
        return false;
    }
}
