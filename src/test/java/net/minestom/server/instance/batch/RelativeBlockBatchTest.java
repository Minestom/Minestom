package net.minestom.server.instance.batch;

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

@EnvTest
public class RelativeBlockBatchTest {
    @Test
    public void basic(Env env) {
        final var instance = env.createFlatInstance();
        ChunkUtils.forChunksInRange(0, 0, 4, (x, z) -> instance.loadChunk(x, z).join());

        final var points = getPoints();
        test(instance, new BlockVec(0, 0, 0), points);
    }

    @Test
    public void offset(Env env) {
        final var instance = env.createFlatInstance();
        ChunkUtils.forChunksInRange(0, 0, 4, (x, z) -> instance.loadChunk(x, z).join());

        final var points = getPoints();
        test(instance, new BlockVec(7, 20, 7), points);
    }

    private void test(Instance instance, Point offset, List<Point> points) {
        final List<Block> originalBlocks = new ArrayList<>();
        final var batch = new RelativeBlockBatch(new BatchOption().setCalculateInverse(true));

        for (Point point : points) {
            originalBlocks.add(instance.getBlock(point.add(offset)));
            batch.setBlock(point, Block.GRASS_BLOCK);
        }

        final var inverse = batch.apply(instance, offset).join();
        assertNotNull(inverse, "Inverse batch null with BatchOption#calculateInverse true");

        for (Point point : points) {
            assertEquals(instance.getBlock(point.add(offset)), Block.GRASS_BLOCK);
        }
        inverse.apply(instance).join();

        for (int index = 0; index < points.size(); index++) {
            assertEquals(originalBlocks.get(index), instance.getBlock(points.get(index)));
        }
    }

    private List<Point> getPoints() {
        final int[] xz = new int[] {-20, -10, 0, 10, 20};
        final List<Point> points = new ArrayList<>();
        for (int x : xz) {
            for (int z : xz) {
                points.add(new BlockVec(x, 4, z));
            }
        }
        return points;
    }
}
