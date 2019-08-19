package fr.themode.minestom.instance;

public abstract class ChunkGenerator {

    public abstract void generateChunkData(ChunkBatch batch, int chunkX, int chunkZ);

    public abstract Biome getBiome(int chunkX, int chunkZ);

}
