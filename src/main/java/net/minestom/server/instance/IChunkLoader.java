package net.minestom.server.instance;

import java.util.function.Consumer;

public interface IChunkLoader {
    boolean loadChunk(Instance instance, int chunkX, int chunkZ, Consumer<Chunk> callback);
    void saveChunk(Chunk chunk, Runnable callback);
}
