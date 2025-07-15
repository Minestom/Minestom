package net.minestom.server.instance;

import org.jspecify.annotations.Nullable;

final class NoopChunkLoaderImpl implements IChunkLoader {
    static final NoopChunkLoaderImpl INSTANCE = new NoopChunkLoaderImpl();

    private NoopChunkLoaderImpl() {
    }

    @Override
    public @Nullable Chunk loadChunk(Instance instance, int chunkX, int chunkZ) {
        return null;
    }

    @Override
    public void saveChunk(Chunk chunk) {
        // Empty
    }
}
