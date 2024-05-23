package net.minestom.server.thread;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@EnvTest
public class AcquirableEntityIntegrationTest {

    @Test
    public void instanceSet(Env env) throws InterruptedException {
        var instance = env.createFlatInstance();
        var zombie = new Entity(EntityType.ZOMBIE);
        CountDownLatch latch = new CountDownLatch(1);
        Thread.startVirtualThread(() -> {
            assertFalse(zombie.acquirable().isOwned());
            assertFalse(zombie.acquirable().isLocal());
            //assertThrows(AcquirableOwnershipException.class, () -> zombie.setInstance(instance, new Pos(1, 41, 1)).join());
            latch.countDown();
        });
        latch.await();
    }
}
