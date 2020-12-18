package net.minestom.server.thread;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceManager;
import org.jetbrains.annotations.NotNull;

public class PerChunkThreadProvider extends ThreadProvider {

    private static final InstanceManager INSTANCE_MANAGER = MinecraftServer.getInstanceManager();

    public PerChunkThreadProvider(int threadCount) {
        super(threadCount);
    }

    @Override
    public void onInstanceCreate(@NotNull Instance instance) {

    }

    @Override
    public void onInstanceDelete(@NotNull Instance instance) {

    }

    @Override
    public void onChunkLoad(@NotNull Instance instance, int chunkX, int chunkZ) {

    }

    @Override
    public void onChunkUnload(@NotNull Instance instance, int chunkX, int chunkZ) {

    }

    @Override
    public void update(long time) {
        for (Instance instance : INSTANCE_MANAGER.getInstances()) {
            // Tick instance
            createBatch(batchHandler -> batchHandler.updateInstance(instance, time), time);

            for (Chunk chunk : instance.getChunks()) {
                // Tick chunks & entities
                createBatch(batchHandler -> {
                    batchHandler.updateChunk(instance, chunk, time);
                    batchHandler.updateEntities(instance, chunk, time);
                }, time);
            }
        }
    }
}
