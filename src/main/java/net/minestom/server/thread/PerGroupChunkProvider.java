package net.minestom.server.thread;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.chunk.ChunkUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

/**
 * Separate chunks into group of linked chunks
 * <p>
 * (1 chunks group = 1 thread execution)
 */
public class PerGroupChunkProvider extends ThreadProvider {

    /**
     * Chunk -> its chunk group
     */
    private final Map<Instance, Long2ObjectMap<LongSet>> instanceChunksGroupMap = new ConcurrentHashMap<>();

    /**
     * Used to know to which instance is linked a Set of chunks
     */
    private final Map<Instance, Map<LongSet, Instance>> instanceInstanceMap = new ConcurrentHashMap<>();

    @Override
    public void onChunkLoad(Instance instance, int chunkX, int chunkZ) {
        final long loadedChunkIndex = ChunkUtils.getChunkIndex(chunkX, chunkZ);

        Long2ObjectMap<LongSet> chunksGroupMap = getChunksGroupMap(instance);
        Map<LongSet, Instance> instanceMap = getInstanceMap(instance);

        // List of groups which are neighbours
        List<LongSet> neighboursGroups = new ArrayList<>();

        final long[] chunks = ChunkUtils.getNeighbours(instance, chunkX, chunkZ);
        boolean findGroup = false;
        for (long chunkIndex : chunks) {
            if (chunksGroupMap.containsKey(chunkIndex)) {
                final LongSet group = chunksGroupMap.get(chunkIndex);
                neighboursGroups.add(group);
                chunksGroupMap.remove(chunkIndex);
                instanceMap.remove(group);
                findGroup = true;
            }
        }

        if (!findGroup) {
            // Create group of one chunk
            LongSet chunkIndexes = new LongArraySet();
            chunkIndexes.add(loadedChunkIndex);

            chunksGroupMap.put(loadedChunkIndex, chunkIndexes);
            instanceMap.put(chunkIndexes, instance);

            return;
        }

        // The size of the final list, used as the initial capacity
        final int size = neighboursGroups.stream().mapToInt(value -> value.size()).sum() + 1;

        // Represent the merged group of all the neighbours
        LongSet finalGroup = new LongArraySet(size);

        // Add the newly loaded chunk to the group
        finalGroup.add(loadedChunkIndex);

        // Add all the neighbours groups to the final one
        for (LongSet chunkCoordinates : neighboursGroups) {
            finalGroup.addAll(chunkCoordinates);
        }

        // Complete maps
        for (long index : finalGroup) {
            chunksGroupMap.put(index, finalGroup);
        }

        instanceMap.put(finalGroup, instance);

    }

    @Override
    public void onChunkUnload(Instance instance, int chunkX, int chunkZ) {
        Long2ObjectMap<LongSet> chunksGroupMap = getChunksGroupMap(instance);
        Map<LongSet, Instance> instanceMap = getInstanceMap(instance);

        final long chunkIndex = ChunkUtils.getChunkIndex(chunkX, chunkZ);
        if (chunksGroupMap.containsKey(chunkIndex)) {
            // The unloaded chunk is part of a group, remove it from the group
            LongSet chunkCoordinates = chunksGroupMap.get(chunkIndex);
            chunkCoordinates.remove(chunkIndex);
            chunksGroupMap.remove(chunkIndex);

            if (chunkCoordinates.isEmpty()) {
                // The chunk group is empty, remove it entirely
                instanceMap.entrySet().removeIf(entry -> entry.getKey().isEmpty());
            }
        }
    }

    @Override
    public List<Future<?>> update(long time) {
        // Set of already-updated instances this tick
        final Set<Instance> updatedInstance = new HashSet<>();

        List<Future<?>> futures = new ArrayList<>();

        instanceInstanceMap.forEach((instance, instanceMap) -> {

            // True if the instance ended its tick call¬
            final CountDownLatch countDownLatch = new CountDownLatch(1);

            // Update all the chunks + instances
            instanceMap.keySet().forEach(chunksIndexes -> {

                final boolean shouldUpdateInstance = updatedInstance.add(instance);
                futures.add(pool.submit(() -> {
                    // Used to check if the instance has already been updated this tick
                    if (shouldUpdateInstance) {
                        updateInstance(instance, time);
                        countDownLatch.countDown();
                    }

                    // Wait for the instance to be updated
                    // Needed because the instance tick is used to unload waiting chunks
                    try {
                        countDownLatch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // Tick all this chunk group
                    chunksIndexes.forEach((long chunkIndex) -> processChunkTick(instance, chunkIndex, time));
                }));
            });
        });
        return futures;
    }

    private Long2ObjectMap<LongSet> getChunksGroupMap(Instance instance) {
        return instanceChunksGroupMap.computeIfAbsent(instance, inst -> new Long2ObjectOpenHashMap<>());
    }

    private Map<LongSet, Instance> getInstanceMap(Instance instance) {
        return instanceInstanceMap.computeIfAbsent(instance, inst -> new HashMap<>());
    }

}
