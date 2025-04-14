package net.minestom.server.thread;

import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

/**
 * Chunks & Entities are elements and Instances are the partitions.
 * Therefore, an instance can only have 1 ticking thread.
 */
public class InstanceBasedThreadDispatcherHandler extends ThreadDispatcherHandler<Instance> {

    @Override
    public @NotNull ThreadProvider<Instance> provider() {
        return ThreadProvider.leastOccupiedThread();
    }

    @Override
    public void onEntityRefreshCurrentChunk(@NotNull Entity entity, @NotNull Chunk currentChunk) {
        // Changing chunk does not change thread for entity, do nothing
    }

    @Override
    public void onEntityRemoval(@NotNull Entity entity) {
        dispatcher.removeElement(entity);
    }

    @Override
    public void onEntitySetInstance(@NotNull Entity entity, @NotNull Instance instance) {
        dispatcher.updateElement(entity, instance);
    }

    @Override
    public void onChunkUnload(@NotNull Chunk chunk) {
        dispatcher.removeElement(chunk);
    }

    @Override
    public void onChunkCached(@NotNull Chunk chunk, @NotNull Instance instance) {
        dispatcher.updateElement(chunk, instance);
    }

    @Override
    public void onInstanceUnregister(@NotNull Instance instance) {
        dispatcher.deletePartition(instance);
    }

    @Override
    public void onInstanceRegister(@NotNull Instance instance) {
        dispatcher.createPartition(instance);
    }
}
