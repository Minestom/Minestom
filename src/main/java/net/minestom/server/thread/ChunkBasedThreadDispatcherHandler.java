package net.minestom.server.thread;

import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

/**
 * Entities are the elements and Chunks are the partitions.
 * Therefore, chunks in the same instance can be running on different ticking threads.
 */
public class ChunkBasedThreadDispatcherHandler extends ThreadDispatcherHandler<Chunk> {

    @Override
    public @NotNull ThreadProvider<Chunk> provider() {
        return ThreadProvider.counter();
    }

    @Override
    public void onEntityRefreshCurrentChunk(@NotNull Entity entity, @NotNull Chunk currentChunk) {
        dispatcher.updateElement(entity, currentChunk);
    }

    @Override
    public void onEntityRemoval(@NotNull Entity entity) {
        dispatcher.removeElement(entity);
    }

    @Override
    public void onEntitySetInstance(@NotNull Entity entity, @NotNull Instance instance) {
        // Nothing required to be done as onEntityRefreshCurrentChunk is also called when entity instance is set which already calls updateElement
    }

    @Override
    public void onChunkUnload(@NotNull Chunk chunk) {
        dispatcher.deletePartition(chunk);
    }

    @Override
    public void onChunkCached(@NotNull Chunk chunk, @NotNull Instance instance) {
        dispatcher.createPartition(chunk);
    }

    @Override
    public void onInstanceUnregister(@NotNull Instance instance) {
        // Nothing required to be done as all chunks will be unloaded when instance is unregistered, deleting all the partitions from onChunkUnload
    }

    @Override
    public void onInstanceRegister(@NotNull Instance instance) {
        instance.getChunks().forEach(dispatcher::createPartition);
    }
}
