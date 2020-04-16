package fr.themode.demo.generator;

import fr.themode.minestom.instance.Biome;
import fr.themode.minestom.instance.ChunkGenerator;
import fr.themode.minestom.instance.batch.ChunkBatch;
import fr.themode.minestom.utils.noise.FastNoise;

import java.util.Random;

public class NoiseTestGenerator extends ChunkGenerator {

    private Random random = new Random();
    private FastNoise fastNoise = new FastNoise();
    private int totalChunk = 15;

    {
        fastNoise.SetNoiseType(FastNoise.NoiseType.Simplex);
        fastNoise.SetInterp(FastNoise.Interp.Linear);
    }

    @Override
    public void generateChunkData(ChunkBatch batch, int chunkX, int chunkZ) {
        for (byte x = 0; x < 16; x++)
            for (byte z = 0; z < 16; z++) {
                float height = fastNoise.GetSimplex(x + 16 * chunkX, z + 16 * chunkZ) * 135;
                height = Math.max(height, 70);
                for (byte y = 0; y < height; y++) {
                    if (random.nextInt(100) > 10) {
                        batch.setCustomBlock(x, y, z, "custom_block");
                    } else {
                        batch.setBlock(x, y, z, (short) 10);
                    }
                }
            }
    }

    @Override
    public Biome getBiome(int chunkX, int chunkZ) {
        return Biome.PLAINS;
    }
}
