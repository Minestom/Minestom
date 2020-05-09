package fr.themode.demo.generator;

import de.articdive.jnoise.JNoise;
import de.articdive.jnoise.interpolation.InterpolationType;
import net.minestom.server.instance.Biome;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.ChunkGenerator;
import net.minestom.server.instance.ChunkPopulator;
import net.minestom.server.instance.batch.ChunkBatch;
import net.minestom.server.instance.block.Block;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class NoiseTestGenerator extends ChunkGenerator {

    private Random random = new Random();
    private JNoise jNoise = JNoise.newBuilder().perlin().setInterpolationType(InterpolationType.LINEAR).setSeed(141414).setFrequency(0.5).build();

    @Override
    public void generateChunkData(ChunkBatch batch, int chunkX, int chunkZ) {
        for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
            for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                double height = jNoise.getNoise((x + chunkX * 16) / 16.0, (z + chunkZ * 16) / 16.0) * 15 + 40;
                for (int y = 0; y < height; y++) {
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
    public void fillBiomes(Biome[] biomes, int chunkX, int chunkZ) {
        Arrays.fill(biomes, Biome.PLAINS);
    }

    @Override
    public List<ChunkPopulator> getPopulators() {
        return null;
    }
}
