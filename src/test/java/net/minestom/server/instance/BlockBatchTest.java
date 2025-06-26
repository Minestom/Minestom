package net.minestom.server.instance;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BlockBatchTest {

    @Test
    public void basic() {
        BlockBatch batch = BlockBatch.explicit(builder -> builder.setBlock(0, 0, 0, Block.STONE));
        assertEquals(Block.STONE, batch.getBlock(0, 0, 0));
    }

    @Test
    public void basicState() {
        Block block = Block.STONE.withNbt(CompoundBinaryTag.builder()
                .putInt("Count", 5)
                .build());
        BlockBatch batch = BlockBatch.explicit(builder -> builder.setBlock(0, 0, 0, block));
        assertEquals(block, batch.getBlock(0, 0, 0));

        batch = BlockBatch.explicitStates(builder -> builder.setBlock(0, 0, 0, block));
        assertEquals(Block.STONE, batch.getBlock(0, 0, 0));
    }
}
