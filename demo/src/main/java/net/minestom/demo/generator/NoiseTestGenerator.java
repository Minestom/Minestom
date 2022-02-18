package net.minestom.demo.generator;

import de.articdive.jnoise.JNoise;
import de.articdive.jnoise.interpolation.InterpolationType;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.ChunkGenerator;
import net.minestom.server.instance.ChunkPopulator;
import net.minestom.server.instance.batch.ChunkBatch;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NoiseTestGenerator implements ChunkGenerator {

    private Random random = new Random();
    private JNoise jNoise = JNoise.newBuilder().perlin().setInterpolation(InterpolationType.LINEAR).setSeed(random.nextInt()).setFrequency(0.4).build();
    private JNoise jNoise2 = JNoise.newBuilder().perlin().setInterpolation(InterpolationType.LINEAR).setSeed(random.nextInt()).setFrequency(0.6).build();
    private TreePopulator treeGen = new TreePopulator();

    public int getHeight(int x, int z) {
        double preHeight = jNoise.getNoise(x / 16.0, z / 16.0);
        return (int) ((preHeight > 0 ? preHeight * 6 : preHeight * 4) + 64);
    }

    @Override
    public void generateChunkData(@NotNull ChunkBatch batch, int chunkX, int chunkZ) {
        for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
            for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                final int height = getHeight(x + chunkX * 16, z + chunkZ * 16);
                for (int y = 0; y < height; y++) {
                    if (y == 0) {
                        batch.setBlock(x, y, z, Block.BEDROCK);
                    } else if (y == height - 1) {
                        batch.setBlock(x, y, z, Block.GRASS_BLOCK);
                    } else if (y > height - 7) {
                        batch.setBlock(x, y, z, Block.DIRT);
                    } else {
                        batch.setBlock(x, y, z, Block.STONE);
                    }
                }
                if (height < 61) {
                    batch.setBlock(x, height - 1, z, Block.DIRT);
                    for (int y = 0; y < 61 - height; y++) {
                        batch.setBlock(x, y + height, z, Block.WATER);
                    }
                }
            }
        }
    }

    @Override
    public List<ChunkPopulator> getPopulators() {
        List<ChunkPopulator> list = new ArrayList<>();
        list.add(treeGen);
        return list;
    }

    private class TreePopulator implements ChunkPopulator {

        final Structure tree;

        public TreePopulator() {
            tree = new Structure();
            tree.addBlock(Block.DIRT, 0, -1, 0);
            tree.addBlock(Block.OAK_LOG, 0, 0, 0);
            tree.addBlock(Block.OAK_LOG, 0, 1, 0);
            tree.addBlock(Block.OAK_LOG, 0, 2, 0);
            tree.addBlock(Block.OAK_LOG, 0, 3, 0);

            tree.addBlock(Block.OAK_LEAVES, 1, 1, 0);
            tree.addBlock(Block.OAK_LEAVES, 2, 1, 0);
            tree.addBlock(Block.OAK_LEAVES, -1, 1, 0);
            tree.addBlock(Block.OAK_LEAVES, -2, 1, 0);

            tree.addBlock(Block.OAK_LEAVES, 1, 1, 1);
            tree.addBlock(Block.OAK_LEAVES, 2, 1, 1);
            tree.addBlock(Block.OAK_LEAVES, 0, 1, 1);
            tree.addBlock(Block.OAK_LEAVES, -1, 1, 1);
            tree.addBlock(Block.OAK_LEAVES, -2, 1, 1);

            tree.addBlock(Block.OAK_LEAVES, 1, 1, 2);
            tree.addBlock(Block.OAK_LEAVES, 2, 1, 2);
            tree.addBlock(Block.OAK_LEAVES, 0, 1, 2);
            tree.addBlock(Block.OAK_LEAVES, -1, 1, 2);
            tree.addBlock(Block.OAK_LEAVES, -2, 1, 2);

            tree.addBlock(Block.OAK_LEAVES, 1, 1, -1);
            tree.addBlock(Block.OAK_LEAVES, 2, 1, -1);
            tree.addBlock(Block.OAK_LEAVES, 0, 1, -1);
            tree.addBlock(Block.OAK_LEAVES, -1, 1, -1);
            tree.addBlock(Block.OAK_LEAVES, -2, 1, -1);

            tree.addBlock(Block.OAK_LEAVES, 1, 1, -2);
            tree.addBlock(Block.OAK_LEAVES, 2, 1, -2);
            tree.addBlock(Block.OAK_LEAVES, 0, 1, -2);
            tree.addBlock(Block.OAK_LEAVES, -1, 1, -2);
            tree.addBlock(Block.OAK_LEAVES, -2, 1, -2);

            tree.addBlock(Block.OAK_LEAVES, 1, 2, 0);
            tree.addBlock(Block.OAK_LEAVES, 2, 2, 0);
            tree.addBlock(Block.OAK_LEAVES, -1, 2, 0);
            tree.addBlock(Block.OAK_LEAVES, -2, 2, 0);

            tree.addBlock(Block.OAK_LEAVES, 1, 2, 1);
            tree.addBlock(Block.OAK_LEAVES, 2, 2, 1);
            tree.addBlock(Block.OAK_LEAVES, 0, 2, 1);
            tree.addBlock(Block.OAK_LEAVES, -1, 2, 1);
            tree.addBlock(Block.OAK_LEAVES, -2, 2, 1);

            tree.addBlock(Block.OAK_LEAVES, 1, 2, 2);
            tree.addBlock(Block.OAK_LEAVES, 2, 2, 2);
            tree.addBlock(Block.OAK_LEAVES, 0, 2, 2);
            tree.addBlock(Block.OAK_LEAVES, -1, 2, 2);
            tree.addBlock(Block.OAK_LEAVES, -2, 2, 2);

            tree.addBlock(Block.OAK_LEAVES, 1, 2, -1);
            tree.addBlock(Block.OAK_LEAVES, 2, 2, -1);
            tree.addBlock(Block.OAK_LEAVES, 0, 2, -1);
            tree.addBlock(Block.OAK_LEAVES, -1, 2, -1);
            tree.addBlock(Block.OAK_LEAVES, -2, 2, -1);

            tree.addBlock(Block.OAK_LEAVES, 1, 2, -2);
            tree.addBlock(Block.OAK_LEAVES, 2, 2, -2);
            tree.addBlock(Block.OAK_LEAVES, 0, 2, -2);
            tree.addBlock(Block.OAK_LEAVES, -1, 2, -2);
            tree.addBlock(Block.OAK_LEAVES, -2, 2, -2);

            tree.addBlock(Block.OAK_LEAVES, 1, 3, 0);
            tree.addBlock(Block.OAK_LEAVES, -1, 3, 0);

            tree.addBlock(Block.OAK_LEAVES, 1, 3, 1);
            tree.addBlock(Block.OAK_LEAVES, 0, 3, 1);
            tree.addBlock(Block.OAK_LEAVES, -1, 3, 1);

            tree.addBlock(Block.OAK_LEAVES, 1, 3, -1);
            tree.addBlock(Block.OAK_LEAVES, 0, 3, -1);
            tree.addBlock(Block.OAK_LEAVES, -1, 3, -1);

            tree.addBlock(Block.OAK_LEAVES, 1, 4, 0);
            tree.addBlock(Block.OAK_LEAVES, 0, 4, 0);
            tree.addBlock(Block.OAK_LEAVES, -1, 4, 0);

            tree.addBlock(Block.OAK_LEAVES, 0, 4, 1);

            tree.addBlock(Block.OAK_LEAVES, 0, 4, -1);
            tree.addBlock(Block.OAK_LEAVES, -1, 4, -1);
        }

        //todo improve
        @Override
        public void populateChunk(ChunkBatch batch, Chunk chunk) {
            for (int i = -2; i < 18; i++) {
                for (int j = -2; j < 18; j++) {
                    if (jNoise2.getNoise(i + chunk.getChunkX() * 16, j + chunk.getChunkZ() * 16) > 0.75) {
                        int y = getHeight(i + chunk.getChunkX() * 16, j + chunk.getChunkZ() * 16);
                        tree.build(batch, new Vec(i, y, j));
                    }
                }
            }
        }


    }

}
