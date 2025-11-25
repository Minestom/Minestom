package net.minestom.server.utils.block;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.batch.AbsoluteBlockBatch;
import net.minestom.server.instance.batch.BatchOption;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@EnvTest
public class BlockBatchTest {

    @Test
    public void inverseConsumerNotNull(Env env) {
        Instance instance = env.createFlatInstance();

        AbsoluteBlockBatch blockBatch = new AbsoluteBlockBatch(new BatchOption().setCalculateInverse(false));
        assertDoesNotThrow(() -> blockBatch.setBlock(0, 0, 0, Block.SNOW));

        CountDownLatch latch = new CountDownLatch(1);
        Assertions.assertTrue(env.tickWhile(() -> latch.getCount() > 0, Duration.ofSeconds(1)));
        blockBatch.apply(instance, (inverse) -> {
            Assertions.assertNull(inverse);
            latch.countDown();
        });
        blockBatch.awaitReady();

        Assertions.assertDoesNotThrow(()->{
            Assertions.assertTrue(latch.await(1, TimeUnit.SECONDS));
        });
    }

    @Test
    public void inverseConsumerNull(Env env) {
        Instance instance = env.createFlatInstance();

        AbsoluteBlockBatch blockBatch = new AbsoluteBlockBatch(new BatchOption());
        assertDoesNotThrow(() -> blockBatch.setBlock(0, 0, 0, Block.SNOW));

        CountDownLatch latch = new CountDownLatch(1);
        Assertions.assertTrue(env.tickWhile(() -> latch.getCount() > 0, Duration.ofSeconds(1)));

        blockBatch.apply(instance, (inverse) -> {
            Assertions.assertNull(inverse);
            latch.countDown();
        });
        Assertions.assertDoesNotThrow(()->{
            Assertions.assertTrue(latch.await(1, TimeUnit.SECONDS));
        });
    }

    @Test
    public void inverseUnsafeConsumerNotNull(Env env) {
        Instance instance = env.createFlatInstance();

        AbsoluteBlockBatch blockBatch = new AbsoluteBlockBatch(new BatchOption().setCalculateInverse(true));
        assertDoesNotThrow(() -> blockBatch.setBlock(0, 0, 0, Block.SNOW));

        CountDownLatch latch = new CountDownLatch(1);
        Assertions.assertTrue(env.tickWhile(() -> latch.getCount() > 0, Duration.ofSeconds(1)));

        blockBatch.unsafeApply(instance, (inverse) ->{
            latch.countDown();
            Assertions.assertNotNull(inverse);
        });
        Assertions.assertDoesNotThrow(()->{
            Assertions.assertTrue(latch.await(1, TimeUnit.SECONDS));
        });
    }

    @Test
    public void inverseUnsafeConsumerNull(Env env) {
        Instance instance = env.createFlatInstance();

        AbsoluteBlockBatch blockBatch = new AbsoluteBlockBatch(new BatchOption());
        assertDoesNotThrow(() -> blockBatch.setBlock(0, 0, 0, Block.SNOW));

        CountDownLatch latch = new CountDownLatch(1);
        Assertions.assertTrue(env.tickWhile(() -> latch.getCount() > 0, Duration.ofSeconds(1)));

        blockBatch.unsafeApply(instance, (inverse) ->{
            Assertions.assertNull(inverse);
            latch.countDown();
        });
        Assertions.assertDoesNotThrow(()->{
            Assertions.assertTrue(latch.await(1, TimeUnit.SECONDS));
        });
    }
}
