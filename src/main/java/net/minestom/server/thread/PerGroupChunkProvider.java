package net.minestom.server.thread;

import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Separate chunks into group of linked chunks
 * <p>
 * (1 chunks group = 1 thread execution)
 */
// FIXME: unusable at the moment, too much overhead because groups need to be created every tick
// Should have a callback for when a chunk is loaded and unloaded, so groups are updated only once
public class PerGroupChunkProvider extends ThreadProvider {

    /**
     * Here are stored all cached chunks waiting for a ChunkGroup
     */
    private Map<Chunk, Set<Chunk>> cachedChunks = new HashMap<>();

    /**
     * Used to know to which instance is linked a Set of chunks
     */
    private Map<Set<Chunk>, Instance> instanceMap = new HashMap<>();

    @Override
    public void start() {
        this.cachedChunks.clear();
        this.instanceMap.clear();
    }

    @Override
    public void linkThread(Instance instance, Chunk chunk) {
        startChunkQuery(instance, chunk.getChunkX(), chunk.getChunkZ());
    }

    @Override
    public void end() {

    }

    @Override
    public void update() {
        // The time of the tick
        final long time = System.currentTimeMillis();

        // Set of already-updated instances
        final Set<Instance> updatedInstance = new HashSet<>();

        // Update all the chunks
        for (Map.Entry<Set<Chunk>, Instance> entry : instanceMap.entrySet()) {
            Set<Chunk> chunks = entry.getKey();
            Instance instance = entry.getValue();

            final boolean updateInstance = updatedInstance.add(instance);
            pool.execute(() -> {
                /*if (updateInstance) {
                    updateInstance(instance, time);
                }

                for (Chunk chunk : chunks) {

                    updateChunk(instance, chunk, time);

                    updateEntities(instance, chunk, time);
                }*/
            });

        }
    }

    /**
     * Check the four chunk neighbors (up/down/left/right)
     * and add them to the cache list
     *
     * @param instance the instance which is checked
     * @param chunkX   the chunk X
     * @param chunkZ   the chunk Z
     */
    private void startChunkQuery(Instance instance, int chunkX, int chunkZ) {
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
    }

}
