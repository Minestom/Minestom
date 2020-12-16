package net.minestom.server.thread;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceManager;
import org.jetbrains.annotations.NotNull;

public class PerElementThreadProvider extends ThreadProvider {

    private static final InstanceManager INSTANCE_MANAGER = MinecraftServer.getInstanceManager();

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
            execute(() -> {
                // Tick instance
                updateInstance(instance, time);

                for (Chunk chunk : instance.getChunks()) {
                    // Tick chunks
                    processChunkTick(instance, chunk, time);
                }
            });
            /*for (Chunk chunk : instance.getChunks()) {
                execute(() -> {
                    // Tick chunks
                    processChunkTick(instance, chunk, time);
                });
            }*/
        }
    }
}
