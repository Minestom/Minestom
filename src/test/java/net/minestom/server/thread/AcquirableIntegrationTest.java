package net.minestom.server.thread;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class AcquirableIntegrationTest {

    @Test
    public void localTest(Env env) {
        var instance = env.createFlatInstance();

        var zombie = new Entity(EntityType.ZOMBIE) {
            @Override
            public void tick(long time) {
                super.tick(time);
                assertTrue(this.getAcquirable().isLocal());
            }
        };
        zombie.setInstance(instance, new Pos(1, 41, 1)).join();
        var acquirable = zombie.getAcquirable();
        // Check local state before initialization
        assertFalse(acquirable.isOwned());
        acquirable.sync(entity -> assertFalse(acquirable.isLocal()));
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
                assertTrue(this.getAcquirable().isOwned());
            }
        };
        zombie.setInstance(instance, new Pos(1, 41, 1)).join();
        var acquirable = zombie.getAcquirable();
        // Check ownership before initialization
        assertFalse(acquirable.isOwned());
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
        var acquirable = zombie.getAcquirable();

        zombie.setInstance(instance, new Pos(1, 41, 1)).join();
        env.tick(); // Init entity

        AtomicInteger counter = new AtomicInteger(0);

        acquirable.sync(entity -> counter.incrementAndGet());
        assertEquals(1, counter.get());

        acquirable.sync(entity -> counter.incrementAndGet());
        assertEquals(2, counter.get());
    }

    @Test
    public void acquireBeforeAfterInit(Env env) {
        // Ensure that acquisition before and after initialization are properly handled
        var instance = env.createFlatInstance();

        var zombie = new Entity(EntityType.ZOMBIE);
        var acquirable = zombie.getAcquirable();

        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch latch2 = new CountDownLatch(1);
        Thread.startVirtualThread(() -> {
            acquirable.sync(entity -> {
                try {
                    latch2.countDown();
                    latch.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        });

        try {
            final boolean success = latch2.await(500, TimeUnit.MILLISECONDS);
            if (!success) fail("Timeout");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        zombie.setInstance(instance, new Pos(1, 41, 1)).join();
        env.tick(); // Init entity

        assertFalse(runTimeout(() -> acquirable.sync(entity -> {
        }), 500, TimeUnit.MILLISECONDS), "Entity has been wrongly acquired twice due to the first acquisition being done before initialization.");

        latch.countDown();

        assertTrue(runTimeout(() -> acquirable.sync(entity -> {
        }), 500, TimeUnit.MILLISECONDS), "Entity didn't get acquired after the pre-init acquire ended.");
    }

    @Test
    public void acquireBeforeInit(Env env) {
        // Ensure that acquisition before initialization are properly handled
        var zombie = new Entity(EntityType.ZOMBIE);
        var acquirable = zombie.getAcquirable();

        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch latch2 = new CountDownLatch(1);
        Thread.startVirtualThread(() -> {
            acquirable.sync(entity -> {
                try {
                    latch2.countDown();
                    latch.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        });

        try {
            final boolean success = latch2.await(500, TimeUnit.MILLISECONDS);
            if (!success) fail("Timeout");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        assertFalse(runTimeout(() -> acquirable.sync(entity -> {
        }), 500, TimeUnit.MILLISECONDS), "Initialization lock failed.");

        latch.countDown();

        assertTrue(runTimeout(() -> acquirable.sync(entity -> {
        }), 500, TimeUnit.MILLISECONDS), "Entity didn't get acquired after the pre-init acquire ended.");
    }

    private boolean runTimeout(Runnable runnable, long timeout, TimeUnit timeUnit) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            executor.submit(runnable).get(timeout, timeUnit);
            return true;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return false;
        }
    }
}
