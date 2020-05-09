package net.minestom.server.instance;

import net.minestom.server.instance.batch.ChunkBatch;

import java.util.List;

public abstract class ChunkGenerator {

    public abstract void generateChunkData(ChunkBatch batch, int chunkX, int chunkZ);

    public abstract void fillBiomes(Biome[] biomes, int chunkX, int chunkZ);

    public abstract List<ChunkPopulator> getPopulators();

}
