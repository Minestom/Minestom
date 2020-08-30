package net.minestom.server.instance;

import net.minestom.server.instance.batch.ChunkBatch;
import net.minestom.server.world.biomes.Biome;

import java.util.List;

public interface ChunkGenerator {

    void generateChunkData(ChunkBatch batch, int chunkX, int chunkZ);

    void fillBiomes(Biome[] biomes, int chunkX, int chunkZ);

    List<ChunkPopulator> getPopulators();

}
