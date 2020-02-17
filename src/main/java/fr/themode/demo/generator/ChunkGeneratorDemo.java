package fr.themode.demo.generator;

import fr.themode.minestom.instance.Biome;
import fr.themode.minestom.instance.ChunkGenerator;
import fr.themode.minestom.instance.batch.ChunkBatch;

import java.util.Random;

public class ChunkGeneratorDemo extends ChunkGenerator {

    private final Random random = new Random();

    @Override
    public void generateChunkData(ChunkBatch batch, int chunkX, int chunkZ) {
        for (byte x = 0; x < 16; x++)
            for (byte z = 0; z < 16; z++) {
                for (byte y = 0; y < 65; y++) {
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
