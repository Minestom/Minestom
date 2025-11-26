package net.minestom.server.utils.block;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.batch.AbsoluteBlockBatch;
import net.minestom.server.instance.batch.BatchOption;
import net.minestom.server.instance.block.Block;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@EnvTest
public class BlockBatchTest {

    @Test
    public void inverseConsumerNotNull(Env env) {
        Instance instance = env.createFlatInstance();

        AbsoluteBlockBatch blockBatch = new AbsoluteBlockBatch(new BatchOption().setCalculateInverse(true));
        blockBatch.setBlock(0, 0, 0, Block.SNOW);

        CountDownLatch latch = new CountDownLatch(1);
        instance.loadChunk(0, 0).join();
        blockBatch.apply(instance, (inverse) -> {
            Assertions.assertNotNull(inverse);
            latch.countDown();
        });
        env.tickWhile(() -> latch.getCount() > 0, Duration.ofSeconds(1));
        try {
            Assertions.assertTrue(latch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void inverseConsumerNull(Env env) {
        Instance instance = env.createFlatInstance();

        AbsoluteBlockBatch blockBatch = new AbsoluteBlockBatch(new BatchOption().setCalculateInverse(false));
        blockBatch.setBlock(0, 0, 0, Block.SNOW);

        CountDownLatch latch = new CountDownLatch(1);
        instance.loadChunk(0, 0).join();
        blockBatch.apply(instance, (inverse) -> {
            Assertions.assertNull(inverse);
            latch.countDown();
        });
        env.tickWhile(() -> latch.getCount() > 0, Duration.ofSeconds(1));
        Assertions.assertDoesNotThrow(()->Assertions.assertTrue(latch.await(1, TimeUnit.SECONDS)));
    }

    @Test
    public void inverseConsumerNotNullUnsafe(Env env) {
        Instance instance = env.createFlatInstance();

        AbsoluteBlockBatch blockBatch = new AbsoluteBlockBatch(new BatchOption().setCalculateInverse(true));
        blockBatch.setBlock(0, 0, 0, Block.SNOW);

        CountDownLatch latch = new CountDownLatch(1);
        instance.loadChunk(0, 0).join();
        blockBatch.unsafeApply(instance, (inverse) -> {
            Assertions.assertNotNull(inverse);
            latch.countDown();
        });
        env.tickWhile(() -> latch.getCount() > 0, Duration.ofSeconds(1));
        Assertions.assertDoesNotThrow(()->Assertions.assertTrue(latch.await(1, TimeUnit.SECONDS)));
    }

    @Test
    public void inverseConsumerNullUnsafe(Env env) {
        Instance instance = env.createFlatInstance();

        AbsoluteBlockBatch blockBatch = new AbsoluteBlockBatch(new BatchOption().setCalculateInverse(false));
        blockBatch.setBlock(0, 0, 0, Block.SNOW);

        CountDownLatch latch = new CountDownLatch(1);
        instance.loadChunk(0, 0).join();
        blockBatch.unsafeApply(instance, (inverse) -> {
            Assertions.assertNull(inverse);
            latch.countDown();
        });
        env.tickWhile(() -> latch.getCount() > 0, Duration.ofSeconds(1));
        Assertions.assertDoesNotThrow(()->Assertions.assertTrue(latch.await(1, TimeUnit.SECONDS)));
    }
}
