package net.minestom.server.instance;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.anvil.AnvilLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.Phaser;

/**
 * Interface implemented to change the way chunks are loaded/saved.
 * <p>
 * See {@link AnvilLoader} for the default implementation used in {@link InstanceContainer}.
 */
public interface IChunkLoader {

    static @NotNull IChunkLoader noop() {
        return NoopChunkLoaderImpl.INSTANCE;
    }

    /**
     * Loads instance data from the loader.
     *
     * @param instance the instance to retrieve the data from
     */
    default void loadInstance(@NotNull Instance instance) {
    }

    /**
     * Loads a {@link Chunk}, all blocks should be set since the {@link net.minestom.server.instance.generator.Generator} is not applied.
     *
     * @param instance the {@link Instance} where the {@link Chunk} belong
     * @param chunkX   the chunk X
     * @param chunkZ   the chunk Z
     * @return the chunk, or null if not present
     */
    @Nullable Chunk loadChunk(@NotNull Instance instance, int chunkX, int chunkZ);

    default void saveInstance(@NotNull Instance instance) {
    }

    /**
     * Saves a {@link Chunk} with an optional callback for when it is done.
     *
     * @param chunk the {@link Chunk} to save
     */
    void saveChunk(@NotNull Chunk chunk);

    /**
     * Saves multiple chunks with an optional callback for when it is done.
     * <p>
     * Implementations need to check {@link #supportsParallelSaving()} to support the feature if possible.
     *
     * @param chunks the chunks to save
     */
    default void saveChunks(@NotNull Collection<Chunk> chunks) {
        if (supportsParallelSaving()) {
            Phaser phaser = new Phaser(1);
            for (Chunk chunk : chunks) {
                phaser.register();
                Thread.startVirtualThread(() -> {
                    try {
                        saveChunk(chunk);
                        phaser.arriveAndDeregister();
                    } catch (Throwable e) {
                        MinecraftServer.getExceptionManager().handleException(e);
                    }
                });
            }
            phaser.arriveAndAwaitAdvance();
        } else {
            for (Chunk chunk : chunks) {
                saveChunk(chunk);
            }
        }
    }

    /**
     * Supports for instance/chunk saving in virtual threads.
     *
     * @return true if the chunk loader supports parallel saving
     */
    default boolean supportsParallelSaving() {
        return false;
    }

    /**
     * Supports for instance/chunk loading in virtual threads.
     *
     * @return true if the chunk loader supports parallel loading
     */
    default boolean supportsParallelLoading() {
        return false;
    }

    /**
     * Called when a chunk is unloaded, so that this chunk loader can unload any resource it is holding.
     * Note: Minestom currently has no way to determine whether the chunk comes from this loader, so you may get
     * unload requests for chunks not created by the loader.
     *
     * @param chunk the chunk to unload
     */
    default void unloadChunk(Chunk chunk) {
    }
}
