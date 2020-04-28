package fr.themode.demo.generator;

import net.minestom.server.instance.Biome;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.ChunkGenerator;
import net.minestom.server.instance.batch.ChunkBatch;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.noise.FastNoise;

import java.util.Random;

public class NoiseTestGenerator extends ChunkGenerator {

    private Random random = new Random();
    private FastNoise fastNoise = new FastNoise();

    {
        fastNoise.SetNoiseType(FastNoise.NoiseType.Simplex);
        fastNoise.SetInterp(FastNoise.Interp.Linear);
    }

    @Override
    public void generateChunkData(ChunkBatch batch, int chunkX, int chunkZ) {
        for (byte x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
            for (byte z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                float height = fastNoise.GetSimplex(x + Chunk.CHUNK_SIZE_X * chunkX, z + Chunk.CHUNK_SIZE_Z * chunkZ) * 135;
                height = Math.max(height, 70);
                for (byte y = 0; y < height; y++) {
                    if (random.nextInt(100) > 10) {
                        batch.setCustomBlock(x, y, z, "custom_block");
                    } else {
                        batch.setBlock(x, y, z, Block.DIAMOND_BLOCK);
                    }
                }
            }
        }
    }

    @Override
    public Biome getBiome(int chunkX, int chunkZ) {
        return Biome.PLAINS;
    }
}
