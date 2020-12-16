package net.minestom.server.thread;

import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Simple thread provider implementation using a single thread to update all the instances and chunks.
 */
public class SingleThreadProvider extends ThreadProvider {

    {
        setThreadCount(1);
    }

    private final Set<Instance> instances = new CopyOnWriteArraySet<>();

    @Override
    public void onInstanceCreate(@NotNull Instance instance) {
        this.instances.add(instance);
    }

    @Override
    public void onInstanceDelete(@NotNull Instance instance) {
        this.instances.remove(instance);
    }

    @Override
    public void onChunkLoad(@NotNull Instance instance, int chunkX, int chunkZ) {

    }

    @Override
    public void onChunkUnload(@NotNull Instance instance, int chunkX, int chunkZ) {

    }

    @Override
    public void update(long time) {
        pool.execute(() -> {
            for (Instance instance : instances) {
                updateInstance(instance, time);
                for (Chunk chunk : instance.getChunks()) {
                    final long index = ChunkUtils.getChunkIndex(chunk.getChunkX(), chunk.getChunkZ());
                    processChunkTick(instance, index, time);
                }
            }
        });
    }
}
