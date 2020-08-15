package net.minestom.server.thread;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.chunk.ChunkUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Separate chunks into group of linked chunks
 * <p>
 * (1 chunks group = 1 thread execution)
 */
public class PerGroupChunkProvider extends ThreadProvider {

    /**
     * Chunk -> its chunk group
     */
    private Map<Instance, Long2ObjectMap<LongSet>> instanceChunksGroupMap = new ConcurrentHashMap<>();

    /**
     * Used to know to which instance is linked a Set of chunks
     */
    private Map<Instance, Map<LongSet, Instance>> instanceInstanceMap = new ConcurrentHashMap<>();

    @Override
    public void onChunkLoad(Instance instance, int chunkX, int chunkZ) {
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
            final long chunkIndex = ChunkUtils.getChunkIndex(chunkX, chunkZ);
            LongSet chunkIndexes = new LongArraySet();
            chunkIndexes.add(chunkIndex);

            chunksGroupMap.put(chunkIndex, chunkIndexes);
            instanceMap.put(chunkIndexes, instance);

            return;
        }

        // Represent the merged group of all the neighbours
        LongSet finalGroup = new LongArraySet();

        // Add the newly loaded chunk to the group
        final long chunkIndex = ChunkUtils.getChunkIndex(chunkX, chunkZ);
        finalGroup.add(chunkIndex);

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
    public ArrayList<Future<?>> update(long time) {
        // Set of already-updated instances this tick
        final Set<Instance> updatedInstance = new HashSet<>();

        ArrayList<Future<?>> futures = new ArrayList<>();

        instanceInstanceMap.forEach((instance, instanceMap) -> {

            // True if the instance ended its tick call
            AtomicBoolean instanceUpdated = new AtomicBoolean(false);

            // Update all the chunks + instances
            for (Map.Entry<LongSet, Instance> ent : instanceMap.entrySet()) {
                final LongSet chunksIndexes = ent.getKey();

                final boolean shouldUpdateInstance = updatedInstance.add(instance);
                futures.add(pool.submit(() -> {
                    // Used to check if the instance has already been updated this tick
                    if (shouldUpdateInstance) {
                        updateInstance(instance, time);
                        instanceUpdated.set(true);
                    }

                    // Wait for the instance to be updated
                    // Needed because the instance tick is used to unload waiting chunks
                    while (!instanceUpdated.get()) {
                    }

                    for (long chunkIndex : chunksIndexes) {
                        final int[] chunkCoordinates = ChunkUtils.getChunkCoord(chunkIndex);
                        final Chunk chunk = instance.getChunk(chunkCoordinates[0], chunkCoordinates[1]);
                        if (!ChunkUtils.isLoaded(chunk)) {
                            continue;
                        }

                        updateChunk(instance, chunk, time);

                        updateEntities(instance, chunk, time);
                    }
                }));

            }

        });
        return futures;
    }

    private Long2ObjectMap<LongSet> getChunksGroupMap(Instance instance) {
        return instanceChunksGroupMap.computeIfAbsent(instance, inst -> new Long2ObjectOpenHashMap<>());
    }

    private Map<LongSet, Instance> getInstanceMap(Instance instance) {
        return instanceInstanceMap.computeIfAbsent(instance, inst -> new ConcurrentHashMap<>());
    }

}
