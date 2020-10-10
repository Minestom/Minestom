package net.minestom.server.utils.chunk;

import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.world.biomes.Biome;

/**
 * Used to customize which type of {@link Chunk} an implementation should use.
 */
@FunctionalInterface
public interface ChunkSupplier {

    /**
     * Create a {@link Chunk} object.
     *
     * @param instance the {@link Instance} assigned to the chunk
     * @param biomes   the biomes of the chunk, can be null
     * @param chunkX   the chunk X
     * @param chunkZ   the chunk Z
     * @return a newly {@link Chunk} object, cannot be null
     */
    Chunk getChunk(Instance instance, Biome[] biomes, int chunkX, int chunkZ);
}
