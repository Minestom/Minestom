package net.minestom.server.instance;

import net.minestom.server.utils.async.AsyncUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

final class NoopChunkLoaderImpl implements IChunkLoader {
    static final NoopChunkLoaderImpl INSTANCE = new NoopChunkLoaderImpl();

    private NoopChunkLoaderImpl(){}

    @Override
    public @NotNull CompletableFuture<@Nullable Chunk> loadChunk(@NotNull Instance instance, int chunkX, int chunkZ) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public @NotNull CompletableFuture<Void> saveChunk(@NotNull Chunk chunk) {
        return AsyncUtils.VOID_FUTURE;
    }
}
