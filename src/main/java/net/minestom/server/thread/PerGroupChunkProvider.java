package net.minestom.server.thread;

import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.chunk.ChunkUtils;

import java.util.*;

/**
 * Separate chunks into group of linked chunks
 * <p>
 * (1 chunks group = 1 thread execution)
 */
public class PerGroupChunkProvider extends ThreadProvider {

    /**
     * Chunk -> its chunk group
     */
    private Map<Instance, Map<ChunkCoordinate, Set<ChunkCoordinate>>> instanceChunksGroupMap = new HashMap<>();

    /**
     * Used to know to which instance is linked a Set of chunks
     */
    private Map<Instance, Map<Set<ChunkCoordinate>, Instance>> instanceInstanceMap = new HashMap<>();

    @Override
    public void onChunkLoad(Instance instance, int chunkX, int chunkZ) {
        Map<ChunkCoordinate, Set<ChunkCoordinate>> chunksGroupMap = getChunksGroupMap(instance);
        Map<Set<ChunkCoordinate>, Instance> instanceMap = getInstanceMap(instance);

        // List of groups which are neighbours
        List<Set<ChunkCoordinate>> neighboursGroups = new ArrayList<>();

        final List<ChunkCoordinate> chunks = getNeighbours(instance, chunkX, chunkZ);
        boolean findGroup = false;
        for (ChunkCoordinate chunkCoordinate : chunks) {
            if (chunksGroupMap.containsKey(chunkCoordinate)) {
                final Set<ChunkCoordinate> group = chunksGroupMap.get(chunkCoordinate);
                neighboursGroups.add(group);
                chunksGroupMap.remove(chunkCoordinate);
                instanceMap.remove(group);
                findGroup = true;
            }
        }

        if (!findGroup) {
            // Create group of one chunk
            final ChunkCoordinate chunkCoordinate = new ChunkCoordinate(chunkX, chunkZ);
            Set<ChunkCoordinate> chunkCoordinates = new HashSet<>();
            chunkCoordinates.add(chunkCoordinate);

            chunksGroupMap.put(chunkCoordinate, chunkCoordinates);
            instanceMap.put(chunkCoordinates, instance);

            return;
        }

        // Represent the merged group of all the neighbours
        Set<ChunkCoordinate> finalGroup = new HashSet<>();

        // Add the newly loaded chunk to the group
        finalGroup.add(new ChunkCoordinate(chunkX, chunkZ));

        // Add all the neighbours groups to the final one
        for (Set<ChunkCoordinate> chunkCoordinates : neighboursGroups) {
            finalGroup.addAll(chunkCoordinates);
        }

        // Complete maps
        for (ChunkCoordinate chunkCoordinate : finalGroup) {
            chunksGroupMap.put(chunkCoordinate, finalGroup);
        }

        instanceMap.put(finalGroup, instance);

    }

    @Override
    public void onChunkUnload(Instance instance, int chunkX, int chunkZ) {
        Map<ChunkCoordinate, Set<ChunkCoordinate>> chunksGroupMap = getChunksGroupMap(instance);
        Map<Set<ChunkCoordinate>, Instance> instanceMap = getInstanceMap(instance);

        final ChunkCoordinate chunkCoordinate = new ChunkCoordinate(chunkX, chunkZ);
        if (chunksGroupMap.containsKey(chunkCoordinate)) {
            // The unloaded chunk is part of a group, remove it from the group
            Set<ChunkCoordinate> chunkCoordinates = chunksGroupMap.get(chunkCoordinate);
            chunkCoordinates.remove(chunkCoordinate);
            chunksGroupMap.remove(chunkCoordinate);

            if (chunkCoordinates.isEmpty()) {
                // The chunk group is empty, remove it entirely
                instanceMap.entrySet().removeIf(entry -> entry.getKey().isEmpty());
            }
        }
    }

    @Override
    public void update(long time) {
        // Set of already-updated instances this tick
        final Set<Instance> updatedInstance = new HashSet<>();

        instanceInstanceMap.entrySet().forEach(entry -> {
            final Instance instance = entry.getKey();
            final Map<Set<ChunkCoordinate>, Instance> instanceMap = entry.getValue();

            // Update all the chunks + instances
            for (Map.Entry<Set<ChunkCoordinate>, Instance> ent : instanceMap.entrySet()) {
                final Set<ChunkCoordinate> chunks = ent.getKey();

                final boolean updateInstance = updatedInstance.add(instance);
                pool.execute(() -> {
                    // Used to check if the instance has already been updated this tick
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

        });
    }

    /**
     * Get all the neighbours of a chunk and itself, no diagonals
     *
     * @param instance the instance of the chunks
     * @param chunkX   the chunk X
     * @param chunkZ   the chunk Z
     * @return the loaded neighbours of the chunk
     */
    private List<ChunkCoordinate> getNeighbours(Instance instance, int chunkX, int chunkZ) {
        List<ChunkCoordinate> chunks = new ArrayList<>();
        // Constants used to loop through the neighbors
        final int[] posX = {1, 0, -1};
        final int[] posZ = {1, 0, -1};

        for (int x : posX) {
            for (int z : posZ) {

                // No diagonal check
                if ((Math.abs(x) + Math.abs(z)) == 2)
                    continue;

                final int targetX = chunkX + x;
                final int targetZ = chunkZ + z;
                final Chunk chunk = instance.getChunk(targetX, targetZ);
                if (!ChunkUtils.isChunkUnloaded(chunk)) {
                    // Chunk is loaded, add it
                    chunks.add(toChunkCoordinate(chunk));
                }

            }
        }
        return chunks;
    }

    private Map<ChunkCoordinate, Set<ChunkCoordinate>> getChunksGroupMap(Instance instance) {
        return instanceChunksGroupMap.computeIfAbsent(instance, inst -> new HashMap<>());
    }

    private Map<Set<ChunkCoordinate>, Instance> getInstanceMap(Instance instance) {
        return instanceInstanceMap.computeIfAbsent(instance, inst -> new HashMap<>());
    }

}
