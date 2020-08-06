package net.minestom.server.thread;

import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.chunk.ChunkUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Separate chunks into group of linked chunks
 * <p>
 * (1 chunks group = 1 thread execution)
 */
// TODO
public class PerGroupChunkProvider extends ThreadProvider {

    /**
     * Here are stored all cached chunks waiting for a ChunkGroup
     */
    private Map<Chunk, Set<ChunkCoordinate>> cachedChunks = new HashMap<>();

    /**
     * Used to know to which instance is linked a Set of chunks
     */
    private Map<Set<ChunkCoordinate>, Instance> instanceMap = new HashMap<>();

    @Override
    public void onChunkLoad(Instance instance, int chunkX, int chunkZ) {

    }

    @Override
    public void onChunkUnload(Instance instance, int chunkX, int chunkZ) {

    }

    @Override
    public void update(long time) {

        // Set of already-updated instances
        final Set<Instance> updatedInstance = new HashSet<>();

        // Update all the chunks
        for (Map.Entry<Set<ChunkCoordinate>, Instance> entry : instanceMap.entrySet()) {
            Set<ChunkCoordinate> chunks = entry.getKey();
            Instance instance = entry.getValue();

            final boolean updateInstance = updatedInstance.add(instance);
            pool.execute(() -> {
                if (updateInstance) {
                    updateInstance(instance, time);
                }

                for (ChunkCoordinate chunkCoordinate : chunks) {
                    final Chunk chunk = instance.getChunk(chunkCoordinate.chunkX, chunkCoordinate.chunkZ);
                    if (ChunkUtils.isChunkUnloaded(chunk)) {
                        continue;
                    }

                    updateChunk(instance, chunk, time);

                    updateEntities(instance, chunk, time);
                }
            });

        }
    }

    /*@Override
    public void linkThread(Instance instance, Chunk chunk) {
        startChunkQuery(instance, chunk.getChunkX(), chunk.getChunkZ());
    }*/

    /**
     * Check the four chunk neighbors (up/down/left/right)
     * and add them to the cache list
     *
     * @param instance the instance which is checked
     * @param chunkX   the chunk X
     * @param chunkZ   the chunk Z
     */
    /*private void startChunkQuery(Instance instance, int chunkX, int chunkZ) {
        // Constants used to loop through the neighbors
        final int[] posX = {1, 0, -1};
        final int[] posZ = {1, 0, -1};

        // The cache which will contain all the current chunk group
        final Set<Chunk> cache = new HashSet<>();

        for (int x : posX) {
            for (int z : posZ) {

                // No diagonal check
                if ((Math.abs(x) + Math.abs(z)) == 2)
                    continue;

                final int targetX = chunkX + x;
                final int targetZ = chunkZ + z;

                final Chunk chunk = instance.getChunk(targetX, targetZ);
                if (cache.contains(chunk)) {
                    continue;
                }

                if (chunk != null) {
                    // If loaded, check if the chunk is already associated with a Set
                    if (cachedChunks.containsKey(chunk)) {
                        // Chunk is associated with a Set, add all them to the updated cache Set
                        Set<Chunk> oldCache = cachedChunks.get(chunk);
                        cache.addAll(oldCache);
                        this.instanceMap.remove(oldCache);
                    } else {
                        // Chunk is alone, add it to the cache list
                        cache.add(chunk);
                    }
                }
            }
        }

        // Add cached chunks into a cache list
        for (Chunk cachedChunk : cache) {
            this.cachedChunks.put(cachedChunk, cache);
        }
        this.instanceMap.put(cache, instance);
    }*/
}
