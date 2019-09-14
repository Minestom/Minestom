package fr.themode.minestom.instance;

import fr.themode.minestom.instance.batch.ChunkBatch;

public abstract class ChunkGenerator {

    public abstract void generateChunkData(ChunkBatch batch, int chunkX, int chunkZ);

    public abstract Biome getBiome(int chunkX, int chunkZ);

}
