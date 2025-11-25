package net.minestom.server.utils.block;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.batch.AbsoluteBlockBatch;
import net.minestom.server.instance.batch.BatchOption;
import net.minestom.server.instance.block.Block;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@EnvTest
public class BlockBatchTest {

    @Test
    public void inverseConsumerNotNull(Env env) {
        Instance instance = env.createFlatInstance();

        AbsoluteBlockBatch blockBatch = new AbsoluteBlockBatch(new BatchOption().setCalculateInverse(true));
        assertDoesNotThrow(() -> blockBatch.setBlock(0, 0, 0, Block.SNOW));

        blockBatch.apply(instance, Assertions::assertNotNull);
    }

    @Test
    public void inverseConsumerNull(Env env) {
        Instance instance = env.createFlatInstance();

        AbsoluteBlockBatch blockBatch = new AbsoluteBlockBatch(new BatchOption());
        assertDoesNotThrow(() -> blockBatch.setBlock(0, 0, 0, Block.SNOW));

        blockBatch.apply(instance, Assertions::assertNull);
    }

    @Test
    public void inverseUnsafeConsumerNotNull(Env env) {
        Instance instance = env.createFlatInstance();

        AbsoluteBlockBatch blockBatch = new AbsoluteBlockBatch(new BatchOption().setCalculateInverse(true));
        assertDoesNotThrow(() -> blockBatch.setBlock(0, 0, 0, Block.SNOW));

        blockBatch.unsafeApply(instance, Assertions::assertNotNull);
    }

    @Test
    public void inverseUnsafeConsumerNull(Env env) {
        Instance instance = env.createFlatInstance();

        AbsoluteBlockBatch blockBatch = new AbsoluteBlockBatch(new BatchOption());
        assertDoesNotThrow(() -> blockBatch.setBlock(0, 0, 0, Block.SNOW));

        blockBatch.apply(instance, Assertions::assertNull);
    }
}
