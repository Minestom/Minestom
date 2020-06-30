package net.minestom.server.instance;

import java.util.function.Consumer;

public interface IChunkLoader {
    void loadOrCreateChunk(Instance instance, int chunkX, int chunkZ, Consumer<Chunk> callback);
    void saveChunk(Chunk chunk, Runnable callback);
}
