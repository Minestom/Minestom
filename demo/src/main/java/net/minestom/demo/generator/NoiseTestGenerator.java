package net.minestom.demo.generator;

import de.articdive.jnoise.generators.noise_parameters.interpolation.Interpolation;
import de.articdive.jnoise.generators.noisegen.opensimplex.FastSimplexNoiseGenerator;
import de.articdive.jnoise.generators.noisegen.perlin.PerlinNoiseGenerator;
import de.articdive.jnoise.pipeline.JNoise;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.Generator;
import org.jetbrains.annotations.NotNull;

public class NoiseTestGenerator implements Generator {

    private final JNoise treeNoise = JNoise.newBuilder()
            .fastSimplex(FastSimplexNoiseGenerator.newBuilder().setSeed(123).build())
            .scale(999)
            .build();

    private final JNoise jNoise = JNoise.newBuilder()
            .perlin(PerlinNoiseGenerator.newBuilder().setSeed(123).setInterpolation(Interpolation.LINEAR).build())
            .scale(0.4).build();

    public int getHeight(int x, int z) {
        double preHeight = jNoise.evaluateNoise(x / 16.0, z / 16.0);
        return (int) ((preHeight > 0 ? preHeight * 6 : preHeight * 4) + 64);
    }

    @Override
    public void generate(@NotNull GenerationUnit unit) {
        Point start = unit.absoluteStart();
        for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
            for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                Point pos;
                {
                    int absX = start.blockX() + x;
                    int absZ = start.blockZ() + z;
                    final int height = getHeight(absX, absZ);
                    pos = new Vec(absX, height, absZ);
                }
                Point posp1 = pos.add(1, 0, 1);

                // Water
                if (pos.y() < 61) {
                    unit.modifier().fill(pos, posp1.withY(61), Block.WATER);
                    unit.modifier().fill(pos.withY(0), posp1, Block.AIR);
                    return;
                }

                // Regular terrain
                unit.modifier().fill(pos.withY(0), posp1, Block.STONE);
                unit.modifier().fill(pos.withY(pos.y() - 7), posp1, Block.DIRT);
                unit.modifier().fill(pos.withY(pos.y() - 1), posp1, Block.GRASS_BLOCK);
                unit.modifier().fill(pos.withY(0), posp1.withY(1), Block.BEDROCK);

                if (treeNoise.evaluateNoise(pos.x(), pos.z()) > 0.8) {
                    TreePopulator.populate(pos, unit);
                }
            }
        }
    }

    private static class TreePopulator {
        private static void populate(Point origin, GenerationUnit unit) {
            unit.fork(setter -> {
                setter.setBlock(origin.add(0, -1, 0), Block.DIRT);
                setter.setBlock(origin.add(0, -1, 0), Block.DIRT);
                setter.setBlock(origin.add(0, 0, 0), Block.OAK_LOG);
                setter.setBlock(origin.add(0, 1, 0), Block.OAK_LOG);
                setter.setBlock(origin.add(0, 2, 0), Block.OAK_LOG);
                setter.setBlock(origin.add(0, 3, 0), Block.OAK_LOG);
                setter.setBlock(origin.add(1, 1, 0), Block.OAK_LEAVES);
                setter.setBlock(origin.add(2, 1, 0), Block.OAK_LEAVES);
                setter.setBlock(origin.add(-1, 1, 0), Block.OAK_LEAVES);
                setter.setBlock(origin.add(-2, 1, 0), Block.OAK_LEAVES);
                setter.setBlock(origin.add(1, 1, 1), Block.OAK_LEAVES);
                setter.setBlock(origin.add(2, 1, 1), Block.OAK_LEAVES);
                setter.setBlock(origin.add(0, 1, 1), Block.OAK_LEAVES);
                setter.setBlock(origin.add(-1, 1, 1), Block.OAK_LEAVES);
                setter.setBlock(origin.add(-2, 1, 1), Block.OAK_LEAVES);
                setter.setBlock(origin.add(1, 1, 2), Block.OAK_LEAVES);
                setter.setBlock(origin.add(2, 1, 2), Block.OAK_LEAVES);
                setter.setBlock(origin.add(0, 1, 2), Block.OAK_LEAVES);
                setter.setBlock(origin.add(-1, 1, 2), Block.OAK_LEAVES);
                setter.setBlock(origin.add(-2, 1, 2), Block.OAK_LEAVES);
                setter.setBlock(origin.add(1, 1, -1), Block.OAK_LEAVES);
                setter.setBlock(origin.add(2, 1, -1), Block.OAK_LEAVES);
                setter.setBlock(origin.add(0, 1, -1), Block.OAK_LEAVES);
                setter.setBlock(origin.add(-1, 1, -1), Block.OAK_LEAVES);
                setter.setBlock(origin.add(-2, 1, -1), Block.OAK_LEAVES);
                setter.setBlock(origin.add(1, 1, -2), Block.OAK_LEAVES);
                setter.setBlock(origin.add(2, 1, -2), Block.OAK_LEAVES);
                setter.setBlock(origin.add(0, 1, -2), Block.OAK_LEAVES);
                setter.setBlock(origin.add(-1, 1, -2), Block.OAK_LEAVES);
                setter.setBlock(origin.add(-2, 1, -2), Block.OAK_LEAVES);
                setter.setBlock(origin.add(1, 2, 0), Block.OAK_LEAVES);
                setter.setBlock(origin.add(2, 2, 0), Block.OAK_LEAVES);
                setter.setBlock(origin.add(-1, 2, 0), Block.OAK_LEAVES);
                setter.setBlock(origin.add(-2, 2, 0), Block.OAK_LEAVES);
                setter.setBlock(origin.add(1, 2, 1), Block.OAK_LEAVES);
                setter.setBlock(origin.add(2, 2, 1), Block.OAK_LEAVES);
                setter.setBlock(origin.add(0, 2, 1), Block.OAK_LEAVES);
                setter.setBlock(origin.add(-1, 2, 1), Block.OAK_LEAVES);
                setter.setBlock(origin.add(-2, 2, 1), Block.OAK_LEAVES);
                setter.setBlock(origin.add(1, 2, 2), Block.OAK_LEAVES);
                setter.setBlock(origin.add(2, 2, 2), Block.OAK_LEAVES);
                setter.setBlock(origin.add(0, 2, 2), Block.OAK_LEAVES);
                setter.setBlock(origin.add(-1, 2, 2), Block.OAK_LEAVES);
                setter.setBlock(origin.add(-2, 2, 2), Block.OAK_LEAVES);
                setter.setBlock(origin.add(1, 2, -1), Block.OAK_LEAVES);
                setter.setBlock(origin.add(2, 2, -1), Block.OAK_LEAVES);
                setter.setBlock(origin.add(0, 2, -1), Block.OAK_LEAVES);
                setter.setBlock(origin.add(-1, 2, -1), Block.OAK_LEAVES);
                setter.setBlock(origin.add(-2, 2, -1), Block.OAK_LEAVES);
                setter.setBlock(origin.add(1, 2, -2), Block.OAK_LEAVES);
                setter.setBlock(origin.add(2, 2, -2), Block.OAK_LEAVES);
                setter.setBlock(origin.add(0, 2, -2), Block.OAK_LEAVES);
                setter.setBlock(origin.add(-1, 2, -2), Block.OAK_LEAVES);
                setter.setBlock(origin.add(-2, 2, -2), Block.OAK_LEAVES);
                setter.setBlock(origin.add(1, 3, 0), Block.OAK_LEAVES);
                setter.setBlock(origin.add(-1, 3, 0), Block.OAK_LEAVES);
                setter.setBlock(origin.add(1, 3, 1), Block.OAK_LEAVES);
                setter.setBlock(origin.add(0, 3, 1), Block.OAK_LEAVES);
                setter.setBlock(origin.add(-1, 3, 1), Block.OAK_LEAVES);
                setter.setBlock(origin.add(1, 3, -1), Block.OAK_LEAVES);
                setter.setBlock(origin.add(0, 3, -1), Block.OAK_LEAVES);
                setter.setBlock(origin.add(-1, 3, -1), Block.OAK_LEAVES);
                setter.setBlock(origin.add(1, 4, 0), Block.OAK_LEAVES);
                setter.setBlock(origin.add(0, 4, 0), Block.OAK_LEAVES);
                setter.setBlock(origin.add(-1, 4, 0), Block.OAK_LEAVES);
                setter.setBlock(origin.add(0, 4, 1), Block.OAK_LEAVES);
                setter.setBlock(origin.add(0, 4, -1), Block.OAK_LEAVES);
                setter.setBlock(origin.add(-1, 4, -1), Block.OAK_LEAVES);
            });
        }


    }

}
