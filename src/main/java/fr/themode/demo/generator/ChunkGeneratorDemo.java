package fr.themode.demo.generator;

import net.minestom.server.instance.Biome;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.ChunkGenerator;
import net.minestom.server.instance.batch.ChunkBatch;
import net.minestom.server.instance.block.Block;

import java.util.Random;

public class ChunkGeneratorDemo extends ChunkGenerator {

    private final Random random = new Random();

    @Override
    public void generateChunkData(ChunkBatch batch, int chunkX, int chunkZ) {
        for (byte x = 0; x < Chunk.CHUNK_SIZE_X; x++)
            for (byte z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                for (byte y = 0; y < 65; y++) {
                    if (random.nextInt(100) > 10) {
                        batch.setCustomBlock(x, y, z, "custom_block");
                    } else {
                        batch.setBlock(x, y, z, Block.DIAMOND_BLOCK);
                    }
                }
            }
    }

    @Override
    public Biome getBiome(int chunkX, int chunkZ) {
        return Biome.PLAINS;
    }
}
