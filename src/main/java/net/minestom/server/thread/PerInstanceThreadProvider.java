package net.minestom.server.thread;

import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.chunk.ChunkUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Separate work between instance (1 instance = 1 thread execution)
 */
public class PerInstanceThreadProvider extends ThreadProvider {

    private Map<Instance, Set<ChunkCoordinate>> instanceChunkMap = new HashMap<>();

    @Override
    public void onChunkLoad(Instance instance, int chunkX, int chunkZ) {
        Set<ChunkCoordinate> chunkCoordinates = instanceChunkMap.computeIfAbsent(instance, inst -> new HashSet<>());
        chunkCoordinates.add(new ChunkCoordinate(chunkX, chunkZ));
    }

    @Override
    public void onChunkUnload(Instance instance, int chunkX, int chunkZ) {
        Set<ChunkCoordinate> chunkCoordinates = instanceChunkMap.computeIfAbsent(instance, inst -> new HashSet<>());

        chunkCoordinates.removeIf(chunkCoordinate -> chunkCoordinate.chunkX == chunkX &&
                chunkCoordinate.chunkZ == chunkZ);

    }

    @Override
    public void update(long time) {
        for (Map.Entry<Instance, Set<ChunkCoordinate>> entry : instanceChunkMap.entrySet()) {
            final Instance instance = entry.getKey();
            final Set<ChunkCoordinate> chunkCoordinates = entry.getValue();

            pool.execute(() -> {
                updateInstance(instance, time);

                for (ChunkCoordinate chunkCoordinate : chunkCoordinates) {
                    final Chunk chunk = instance.getChunk(chunkCoordinate.chunkX, chunkCoordinate.chunkZ);
                    if (ChunkUtils.isChunkUnloaded(chunk))
                        continue;

                    updateChunk(instance, chunk, time);

                    updateEntities(instance, chunk, time);

                }
            });

        }
    }

}
