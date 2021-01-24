package net.minestom.server.instance;

import net.minestom.server.MinecraftServer;
import net.minestom.server.utils.callback.OptionalCallback;
import net.minestom.server.utils.chunk.ChunkCallback;
import net.minestom.server.utils.thread.MinestomThread;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Interface implemented to change the way chunks are loaded/saved.
 * <p>
 * See {@link MinestomBasicChunkLoader} for the default implementation used in {@link InstanceContainer}.
 */
public interface IChunkLoader {

    /**
     * Loads a {@link Chunk}, all blocks should be set since the {@link ChunkGenerator} is not applied.
     *
     * @param instance the {@link Instance} where the {@link Chunk} belong
     * @param chunkX   the chunk X
     * @param chunkZ   the chunk Z
     * @param callback the callback executed when the {@link Chunk} is done loading,
     *                 never called if the method returns false. Can be null.
     * @return true if the chunk loaded successfully, false otherwise
     */
    boolean loadChunk(@NotNull Instance instance, int chunkX, int chunkZ, @Nullable ChunkCallback callback);

    /**
     * Saves a {@link Chunk} with an optional callback for when it is done.
     *
     * @param chunk    the {@link Chunk} to save
     * @param callback the callback executed when the {@link Chunk} is done saving,
     *                 should be called even if the saving failed (you can throw an exception).
     *                 Can be null.
     */
    void saveChunk(@NotNull Chunk chunk, @Nullable Runnable callback);

    /**
     * Saves multiple chunks with an optional callback for when it is done.
     * <p>
     * Implementations need to check {@link #supportsParallelSaving()} to support the feature if possible.
     *
     * @param chunks   the chunks to save
     * @param callback the callback executed when the {@link Chunk} is done saving,
     *                 should be called even if the saving failed (you can throw an exception).
     *                 Can be null.
     */
    default void saveChunks(@NotNull Collection<Chunk> chunks, @Nullable Runnable callback) {
        if (supportsParallelSaving()) {
            ExecutorService parallelSavingThreadPool = new MinestomThread(MinecraftServer.THREAD_COUNT_PARALLEL_CHUNK_SAVING, MinecraftServer.THREAD_NAME_PARALLEL_CHUNK_SAVING, true);
            chunks.forEach(c -> parallelSavingThreadPool.execute(() -> saveChunk(c, null)));
            try {
                parallelSavingThreadPool.shutdown();
                parallelSavingThreadPool.awaitTermination(1L, java.util.concurrent.TimeUnit.DAYS);
                OptionalCallback.execute(callback);
            } catch (InterruptedException e) {
                MinecraftServer.getExceptionManager().handleException(e);
            }
        } else {
            AtomicInteger counter = new AtomicInteger();
            for (Chunk chunk : chunks) {
                saveChunk(chunk, () -> {
                    final boolean isLast = counter.incrementAndGet() == chunks.size();
                    if (isLast) {
                        OptionalCallback.execute(callback);
                    }
                });
            }
        }
    }

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
