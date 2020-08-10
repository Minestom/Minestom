package net.minestom.server.thread;

import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minestom.server.instance.Chunk;
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
        for (Map.Entry<Instance, LongSet> entry : instanceChunkMap.entrySet()) {
            final Instance instance = entry.getKey();
            final LongSet chunkIndexes = entry.getValue();

            pool.execute(() -> {
                updateInstance(instance, time);

                for (long chunkIndex : chunkIndexes) {
                    final int[] chunkCoordinates = ChunkUtils.getChunkCoord(chunkIndex);
                    final Chunk chunk = instance.getChunk(chunkCoordinates[0], chunkCoordinates[1]);
                    if (!ChunkUtils.isLoaded(chunk))
                        continue;

                    updateChunk(instance, chunk, time);

                    updateEntities(instance, chunk, time);

                }
            });
        }
    }

    private LongSet getChunkCoordinates(Instance instance) {
        return instanceChunkMap.computeIfAbsent(instance, inst -> new LongArraySet());
    }

}
