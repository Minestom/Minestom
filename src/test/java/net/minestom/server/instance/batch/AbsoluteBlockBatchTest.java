package net.minestom.server.instance.batch;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// These tests also cover ChunkBatch
@EnvTest
public class AbsoluteBlockBatchTest {
    @Test
    public void singleChunk(Env env) {
        final var instance = env.createFlatInstance();
        instance.loadChunk(0, 0).join();

        // one in air and one in stone
        testPoint(instance, new BlockVec(0, 50, 0));
        testPoint(instance, new BlockVec(0, 25, 0));
    }

    private void testPoint(Instance instance, Point point) {
        final var batch = new AbsoluteBlockBatch(new BatchOption().setCalculateInverse(true));
        batch.setBlock(point, Block.GRASS_BLOCK);
        final var originalBlock = instance.getBlock(point);

        final var inverse = batch.apply(instance).join();
        assertNotNull(inverse, "Inverse batch null with BatchOption#calculateInverse true");
        assertEquals(instance.getBlock(point), Block.GRASS_BLOCK);

        inverse.apply(instance).join();
        assertEquals(originalBlock, instance.getBlock(point));
    }

    @Test
    public void multipleChunks(Env env) {
        final var instance = env.createFlatInstance();
        ChunkUtils.forChunksInRange(0, 0, 2, (x, z) -> instance.loadChunk(x, z).join());

        // generate list of points
        final int[] horizontalLocations = new int[] {0, 25, -5, -25};
        final int[] heights = new int[] {25, 50, -5, -25};
        final List<Point> points = new ArrayList<>();

        for (int x : horizontalLocations) {
            for (int y : heights) {
                for (int z : horizontalLocations) {
                    points.add(new BlockVec(x, y, z));
                }
            }
        }

        final var batch = new AbsoluteBlockBatch(new BatchOption().setCalculateInverse(true));

        final List<Block> originalBlocks = new ArrayList<>();
        for (Point point : points) {
            originalBlocks.add(instance.getBlock(point));
            batch.setBlock(point, Block.GRASS_BLOCK);
        }

        final var inverse = batch.apply(instance).join();
        assertNotNull(inverse, "Inverse batch null with BatchOption#calculateInverse true");

        for (Point point : points) {
            assertEquals(instance.getBlock(point), Block.GRASS_BLOCK);
        }

        inverse.apply(instance).join();
        for (int index = 0; index < points.size(); index++) {
            assertEquals(originalBlocks.get(index), instance.getBlock(points.get(index)));
        }
    }

    @Test
    public void withNbt(Env env) {
        final var instance = env.createFlatInstance();
        instance.loadChunk(0, 0).join();

        final Block originalBlock = Block.GRASS_BLOCK.withNbt(CompoundBinaryTag.builder().putBoolean("original", true).build());
        final Block newBlock = Block.GRASS_BLOCK.withNbt(CompoundBinaryTag.builder().putBoolean("original", false).build());
        instance.setBlock(0, 50, 0, originalBlock);

        final var batch = new AbsoluteBlockBatch(new BatchOption().setCalculateInverse(true));
        batch.setBlock(0, 50, 0, newBlock);

        final var inverse = batch.apply(instance).join();
        assertNotNull(inverse, "Inverse batch null with BatchOption#calculateInverse true");

        assertEquals(newBlock, instance.getBlock(0, 50, 0));
        inverse.apply(instance).join();

        assertEquals(originalBlock, instance.getBlock(0, 50, 0));
    }
}
