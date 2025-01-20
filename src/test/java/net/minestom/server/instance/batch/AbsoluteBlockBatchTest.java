package net.minestom.server.instance.batch;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// These tests also cover ChunkBatch
@EnvTest
class AbsoluteBlockBatchTest {
    @Test
    public void basic(Env env) {
        final var instance = env.createFlatInstance();
        for (int x = -2; x < 2; x++) {
            for (int z = -2; z < 2; z++) {
                instance.loadChunk(x, z).join();
            }
        }

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
            assertEquals(Block.GRASS_BLOCK, instance.getBlock(point));
        }

        inverse.apply(instance).join();
        for (int index = 0; index < points.size(); index++) {
            assertEquals(originalBlocks.get(index), instance.getBlock(points.get(index)));
        }
    }
}
