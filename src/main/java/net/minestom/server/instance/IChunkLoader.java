package net.minestom.server.instance;

import java.util.function.Consumer;

public interface IChunkLoader {
    boolean loadChunk(Instance instance, int chunkX, int chunkZ, Consumer<Chunk> callback);
    void saveChunk(Chunk chunk, Runnable callback);

    /**
     * Does this ChunkLoader allow for multithreaded saving of chunks?
     * @return
     */
    default boolean supportsParallelSaving() {
        return false;
    }

    /**
     * Does this ChunkLoader allow for multithreaded loading of chunks?
     * @return
     */
    default boolean supportsParallelLoading() {
        return false;
    }
}
