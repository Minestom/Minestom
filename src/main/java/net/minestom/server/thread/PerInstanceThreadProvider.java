package net.minestom.server.thread;

import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * Separates work between instance (1 instance = 1 thread execution).
 */
public class PerInstanceThreadProvider extends ThreadProvider {

    private final Map<Instance, Set<Chunk>> instanceChunkMap = new ConcurrentHashMap<>();

    @Override
    public void onInstanceCreate(@NotNull Instance instance) {
        this.instanceChunkMap.putIfAbsent(instance, ConcurrentHashMap.newKeySet());
    }

    @Override
    public void onInstanceDelete(@NotNull Instance instance) {
        this.instanceChunkMap.remove(instance);
    }

    @Override
    public void onChunkLoad(@NotNull Instance instance, @NotNull Chunk chunk) {
        // Add the loaded chunk to the instance chunks list
        Set<Chunk> chunks = getChunks(instance);
        chunks.add(chunk);
    }

    @Override
    public void onChunkUnload(@NotNull Instance instance, @NotNull Chunk chunk) {
        Set<Chunk> chunks = getChunks(instance);
        chunks.remove(chunk);
    }

    @NotNull
    @Override
    public List<Future<?>> update(long time) {
        List<Future<?>> futures = new ArrayList<>();

        instanceChunkMap.forEach((instance, chunks) -> futures.add(pool.submit(() -> {
            // Tick instance
            updateInstance(instance, time);
            // Tick chunks
            for (Chunk chunk : chunks) {
                processChunkTick(instance, chunk, time);
            }
        })));
        return futures;
    }

    private Set<Chunk> getChunks(Instance instance) {
        return instanceChunkMap.computeIfAbsent(instance, inst -> ConcurrentHashMap.newKeySet());
    }

}
