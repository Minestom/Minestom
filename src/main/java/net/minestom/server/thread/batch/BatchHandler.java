package net.minestom.server.thread.batch;

import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.SharedInstance;
import net.minestom.server.utils.callback.validator.EntityValidator;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface BatchHandler {

    // INSTANCE UPDATE

    /**
     * Executes an instance tick.
     *
     * @param instance the instance
     * @param time     the current time in ms
     */
    void updateInstance(@NotNull Instance instance, long time);

    /**
     * Executes a chunk tick (blocks update).
     *
     * @param instance the chunk's instance
     * @param chunk    the chunk
     * @param time     the current time in ms
     */
    void updateChunk(@NotNull Instance instance, @NotNull Chunk chunk, long time);

    /**
     * Processes a whole tick for a chunk.
     *
     * @param instance   the instance of the chunk
     * @param chunkIndex the index of the chunk {@link ChunkUtils#getChunkIndex(int, int)}
     * @param time       the time of the update in milliseconds
     */
    default void updateChunk(@NotNull Instance instance, long chunkIndex, long time) {
        final int chunkX = ChunkUtils.getChunkCoordX(chunkIndex);
        final int chunkZ = ChunkUtils.getChunkCoordZ(chunkIndex);

        final Chunk chunk = instance.getChunk(chunkX, chunkZ);
        updateChunk(instance, chunk, time);
    }

    // ENTITY UPDATE

    /**
     * Executes an entity tick (all entities type creatures/objects/players) in an instance's chunk.
     *
     * @param instance the chunk's instance
     * @param chunk    the chunk
     * @param time     the current time in ms
     */
    default void updateEntities(@NotNull Instance instance, @NotNull Chunk chunk, long time) {
        conditionalEntityUpdate(instance, chunk, time, null);
    }

    /**
     * Executes an entity tick in an instance's chunk if condition is verified.
     *
     * @param instance  the chunk's instance
     * @param chunk     the chunk
     * @param time      the current time in ms
     * @param condition the condition which confirm if the update happens or not
     */
    void conditionalEntityUpdate(@NotNull Instance instance,
                                 @NotNull Chunk chunk, long time,
                                 @Nullable EntityValidator condition);

    default boolean shouldTick(@NotNull Entity entity, @Nullable EntityValidator condition) {
        return condition == null || condition.isValid(entity);
    }

    /**
     * If {@code instance} is an {@link InstanceContainer}, run a callback for all of its
     * {@link SharedInstance}.
     *
     * @param instance the instance
     * @param callback the callback to run for all the {@link SharedInstance}
     */
    default void updateSharedInstances(@NotNull Instance instance, @NotNull Consumer<SharedInstance> callback) {
        if (instance instanceof InstanceContainer) {
            final InstanceContainer instanceContainer = (InstanceContainer) instance;

            if (!instanceContainer.hasSharedInstances())
                return;

            for (SharedInstance sharedInstance : instanceContainer.getSharedInstances()) {
                callback.accept(sharedInstance);
            }
        }
    }

}