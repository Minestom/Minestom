package net.minestom.server.instance;

import net.minestom.server.MinecraftServer;
import net.minestom.server.utils.async.AsyncUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Interface implemented to change the way chunks are loaded/saved.
 * <p>
 * See {@link AnvilLoader} for the default implementation used in {@link InstanceContainer}.
 */
public interface IChunkLoader {

    /**
     * Loads instance data from the loader.
     *
     * @param instance the instance to retrieve the data from
     */
    default void loadInstance(@NotNull Instance instance) {
    }

    /**
     * Loads a {@link Chunk}, all blocks should be set since the {@link ChunkGenerator} is not applied.
     *
     * @param instance the {@link Instance} where the {@link Chunk} belong
     * @param chunkX   the chunk X
     * @param chunkZ   the chunk Z
     * @return a {@link CompletableFuture} containing the chunk, or null if not present
     */
    @NotNull CompletableFuture<@Nullable Chunk> loadChunk(@NotNull Instance instance, int chunkX, int chunkZ);

    default @NotNull CompletableFuture<Void> saveInstance(@NotNull Instance instance) {
        return AsyncUtils.VOID_FUTURE;
    }

    /**
     * Saves a {@link Chunk} with an optional callback for when it is done.
     *
     * @param chunk the {@link Chunk} to save
     * @return a {@link CompletableFuture} executed when the {@link Chunk} is done saving,
     * should be called even if the saving failed (you can throw an exception).
     */
    @NotNull CompletableFuture<Void> saveChunk(@NotNull Chunk chunk);

    /**
     * Saves multiple chunks with an optional callback for when it is done.
     * <p>
     * Implementations need to check {@link #supportsParallelSaving()} to support the feature if possible.
     *
     * @param chunks the chunks to save
     * @return a {@link CompletableFuture} executed when the {@link Chunk} is done saving,
     * should be called even if the saving failed (you can throw an exception).
     */
    default @NotNull CompletableFuture<Void> saveChunks(@NotNull Collection<Chunk> chunks) {
        if (supportsParallelSaving()) {
            ExecutorService parallelSavingThreadPool = ForkJoinPool.commonPool();
            chunks.forEach(c -> parallelSavingThreadPool.execute(() -> saveChunk(c)));
            try {
                parallelSavingThreadPool.shutdown();
                parallelSavingThreadPool.awaitTermination(1L, java.util.concurrent.TimeUnit.DAYS);
            } catch (InterruptedException e) {
                MinecraftServer.getExceptionManager().handleException(e);
            }
            return AsyncUtils.VOID_FUTURE;
        } else {
            CompletableFuture<Void> completableFuture = new CompletableFuture<>();
            AtomicInteger counter = new AtomicInteger();
            for (Chunk chunk : chunks) {
                saveChunk(chunk).whenComplete((unused, throwable) -> {
                    final boolean isLast = counter.incrementAndGet() == chunks.size();
                    if (isLast) {
                        completableFuture.complete(null);
                    }
                });
            }
            return completableFuture;
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
