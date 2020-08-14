package net.minestom.server.thread;

import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.chunk.ChunkUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Separate work between instance (1 instance = 1 thread execution)
 */
public class PerInstanceThreadProvider extends ThreadProvider {

    private Map<Instance, LongSet> instanceChunkMap = new HashMap<>();

    @Override
    public void onChunkLoad(Instance instance, int chunkX, int chunkZ) {
        // Add the loaded chunk to the instance chunks list
        LongSet chunkCoordinates = getChunkCoordinates(instance);
        final long index = ChunkUtils.getChunkIndex(chunkX, chunkZ);
        chunkCoordinates.add(index);
    }

    @Override
    public void onChunkUnload(Instance instance, int chunkX, int chunkZ) {
        LongSet chunkCoordinates = getChunkCoordinates(instance);
        final long index = ChunkUtils.getChunkIndex(chunkX, chunkZ);
        // Remove the unloaded chunk from the instance list
        chunkCoordinates.remove(index);

    }

    @Override
    public void update(long time) {
        instanceChunkMap.forEach((instance, chunkIndexes) -> {
            pool.execute(() -> {
                // Tick instance
                updateInstance(instance, time);
                // Tick chunks
                chunkIndexes.forEach((long chunkIndex) -> processChunkTick(instance, chunkIndex, time));
            });
        });
    }

    private LongSet getChunkCoordinates(Instance instance) {
        return instanceChunkMap.computeIfAbsent(instance, inst -> new LongArraySet());
    }

}
