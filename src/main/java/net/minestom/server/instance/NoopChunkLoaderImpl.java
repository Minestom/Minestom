package net.minestom.server.instance;

import org.jetbrains.annotations.Nullable;

record NoopChunkLoaderImpl() implements ChunkLoader {
    static final NoopChunkLoaderImpl INSTANCE = new NoopChunkLoaderImpl();

    @Override
    public @Nullable Chunk loadChunk(Instance instance, int chunkX, int chunkZ) {
        return null;
    }

    @Override
    public void saveChunk(Chunk chunk) {
        // Empty
    }

    @Override
    public boolean supportsParallelLoading() {
        return true;
    }

    @Override
    public boolean supportsParallelSaving() {
        return true;
    }
}
