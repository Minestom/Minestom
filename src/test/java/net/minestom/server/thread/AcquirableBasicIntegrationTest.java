package net.minestom.server.thread;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class AcquirableBasicIntegrationTest {

    @Test
    public void localTest(Env env) {
        var instance = env.createFlatInstance();

        var zombie = new Entity(EntityType.ZOMBIE) {
            @Override
            public void tick(long time) {
                super.tick(time);
                assertTrue(this.acquirable().isLocal());
            }
        };
        zombie.setInstance(instance, new Pos(1, 41, 1)).join();
        var acquirable = zombie.acquirable();
        // Check local state before initialization
        assertTrue(acquirable.isOwned());
        acquirable.sync(entity -> assertTrue(acquirable.isLocal()));
        Thread.startVirtualThread(() -> assertFalse(acquirable.isLocal()));

        env.tick(); // Ensure the entity can access itself

        // Check local state after initialization
        assertFalse(acquirable.isOwned());
        acquirable.sync(entity -> assertFalse(acquirable.isLocal()));
        Thread.startVirtualThread(() -> assertFalse(acquirable.isLocal()));
    }

    @Test
    public void ownedTest(Env env) {
        var instance = env.createFlatInstance();

        var zombie = new Entity(EntityType.ZOMBIE) {
            @Override
            public void tick(long time) {
                super.tick(time);
                assertTrue(this.acquirable().isOwned());
            }
        };
        zombie.setInstance(instance, new Pos(1, 41, 1)).join();
        var acquirable = zombie.acquirable();
        // Check ownership before initialization
        assertTrue(acquirable.isOwned());
        acquirable.sync(entity -> assertTrue(acquirable.isOwned()));
        Thread.startVirtualThread(() -> assertFalse(acquirable.isOwned()));

        env.tick(); // Ensure the entity can access itself

        // Check ownership after initialization
        assertFalse(acquirable.isOwned());
        acquirable.sync(entity -> assertTrue(acquirable.isOwned()));
        Thread.startVirtualThread(() -> assertFalse(acquirable.isOwned()));
    }

    @Test
    public void acquireSingleThreadInit(Env env) {
        // Ensure that acquisition before and after initialization are properly handled
        var instance = env.createFlatInstance();

        var zombie = new Entity(EntityType.ZOMBIE);
        var acquirable = zombie.acquirable();

        zombie.setInstance(instance, new Pos(1, 41, 1)).join();
        env.tick(); // Init entity

        AtomicInteger counter = new AtomicInteger(0);

        acquirable.sync(entity -> counter.incrementAndGet());
        assertEquals(1, counter.get());

        acquirable.sync(entity -> counter.incrementAndGet());
        assertEquals(2, counter.get());
    }

    @Test
    public void acquireBeforeInit(Env env) throws InterruptedException {
        // Ensure that acquisition before initialization are properly handled
        var zombie = new Entity(EntityType.ZOMBIE);
        var acquirable = zombie.acquirable();
        CountDownLatch latch = new CountDownLatch(1);
        Thread.startVirtualThread(() -> assertThrows(IllegalStateException.class, () -> {
            latch.countDown();
            acquirable.sync(entity -> {
            });
        }));
        latch.await();
    }
}
