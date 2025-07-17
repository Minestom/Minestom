package net.minestom.server.instance;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class NoopChunkLoaderImpl implements IChunkLoader {
    static final NoopChunkLoaderImpl INSTANCE = new NoopChunkLoaderImpl();

    private NoopChunkLoaderImpl() {
    }

    @Override
    public @Nullable Chunk loadChunk(@NotNull Instance instance, int chunkX, int chunkZ) {
        return null;
    }

    @Override
    public void saveChunk(@NotNull Chunk chunk) {
        // Empty
    }
}
