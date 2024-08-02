package net.minestom.server.instance.batch;

import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RelativeBlockBatchTest {

    @Test
    public void testReadWrite() {
        RelativeBlockBatch batch = new RelativeBlockBatch();
        batch.setBlock(0, 0, 0, Block.STONE);
        assertEquals(Block.STONE, batch.getBlock(0, 0, 0));

        batch.setBlock(0, 0, 0, Block.GRASS_BLOCK);
        assertEquals(Block.GRASS_BLOCK, batch.getBlock(0, 0, 0));

        batch.setBlock(0, 1, 0, Block.AIR);
        assertEquals(Block.AIR, batch.getBlock(0, 1, 0));

        batch.setBlock(0, 1, 0, Block.STONE);
        assertEquals(Block.STONE, batch.getBlock(0, 1, 0));

        batch.setBlock(1000, 1, 0, Block.GRASS_BLOCK);
        assertEquals(Block.GRASS_BLOCK, batch.getBlock(1000, 1, 0));
    }

    @Test
    public void testJoinOverwrite() {
        RelativeBlockBatch batch1 = new RelativeBlockBatch();
        RelativeBlockBatch batch2 = new RelativeBlockBatch();

        batch1.setBlock(0, 0, 0, Block.STONE);
        batch2.setBlock(0, 0, 0, Block.GRASS_BLOCK);

        batch1.join(batch2);
        assertEquals(Block.GRASS_BLOCK, batch1.getBlock(0, 0, 0));
    }

    @Test
    public void testJoinNoOverwrite() {
        RelativeBlockBatch batch1 = new RelativeBlockBatch();
        RelativeBlockBatch batch2 = new RelativeBlockBatch();

        batch1.setBlock(0, 0, 0, Block.STONE);
        batch2.setBlock(0, 1, 0, Block.GRASS_BLOCK);

        batch1.join(batch2);
        assertEquals(Block.STONE, batch1.getBlock(0, 0, 0));
        assertEquals(Block.GRASS_BLOCK, batch1.getBlock(0, 1, 0));
    }

    @Test
    public void testDiff() {
        RelativeBlockBatch batch1 = new RelativeBlockBatch();
        RelativeBlockBatch batch2 = new RelativeBlockBatch();

        batch1.setBlock(0, 0, 0, Block.STONE);
        batch1.setBlock(0, 2, 0, Block.STONE);
        batch2.setBlock(0, 0, 0, Block.GRASS_BLOCK);
        batch2.setBlock(0, 1, 0, Block.GRASS_BLOCK);

        RelativeBlockBatch diff = batch2.diff(batch1);
        assertEquals(Block.GRASS_BLOCK, diff.getBlock(0, 1, 0));
        assertEquals(Block.GRASS_BLOCK, diff.getBlock(0, 0, 0));
        assertEquals(Block.AIR, diff.getBlock(0, 2, 0));
    }
}