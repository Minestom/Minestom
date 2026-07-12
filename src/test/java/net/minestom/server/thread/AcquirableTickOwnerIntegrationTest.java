package net.minestom.server.thread;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class AcquirableTickOwnerIntegrationTest {

    @Test
    public void ownerRouting(Env env) throws InterruptedException {
        var instance = env.createFlatInstance();
        var owner = new TestOwner();
        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        entity.setExternallyTicked(owner);
        var acquirable = entity.acquirable();

        assertFalse(acquirable.isLocal());
        assertFalse(acquirable.isOwned());
        acquirable.sync(e -> assertTrue(acquirable.isOwned()));

        // On the owner's thread the element is local: sync is free
        var done = new CountDownLatch(1);
        Thread.startVirtualThread(() -> {
            owner.thread = Thread.currentThread();
            assertTrue(acquirable.isLocal());
            acquirable.sync(e -> assertTrue(acquirable.isLocal()));
            done.countDown();
        });
        assertTrue(done.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void ownerBlocking(Env env) throws InterruptedException {
        var instance = env.createFlatInstance();
        var owner = new TestOwner();
        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        entity.setExternallyTicked(owner);

        // A sync must wait while the owner's loop holds its lock
        owner.lock.lock();
        var acquired = new CountDownLatch(1);
        Thread.startVirtualThread(() -> entity.acquirable().sync(e -> acquired.countDown()));
        assertFalse(acquired.await(200, TimeUnit.MILLISECONDS), "Sync must block while the owner ticks");
        owner.lock.unlock();
        assertTrue(acquired.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void ownerGuard(Env env) {
        var instance = env.createFlatInstance();
        var owner = new TestOwner();
        var ticks = new AtomicInteger();
        var entity = new Entity(EntityType.ZOMBIE) {
            @Override
            public void update(long time) {
                ticks.incrementAndGet();
            }
        };
        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        entity.setExternallyTicked(owner);

        entity.tick(System.currentTimeMillis()); // Not the owner's thread: ignored
        assertEquals(0, ticks.get());

        owner.thread = Thread.currentThread();
        entity.tick(System.currentTimeMillis());
        assertEquals(1, ticks.get());
    }

    @Test
    public void ownerHandback(Env env) {
        var instance = env.createFlatInstance();
        var owner = new TestOwner();
        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 42, 0)).join();
        entity.setExternallyTicked(owner);
        assertSame(owner, entity.getTickOwner());

        entity.setExternallyTicked(false);
        assertNull(entity.getTickOwner());
        env.tick(); // Re-registration assigns a tick thread again
        assertNotNull(entity.acquirable().assignedThread());
    }

    static final class TestOwner implements TickOwner {
        final ReentrantLock lock = new ReentrantLock();
        volatile Thread thread;

        @Override
        public ReentrantLock lock() {
            return lock;
        }

        @Override
        public boolean isCurrentThread() {
            return Thread.currentThread() == thread;
        }
    }
}
