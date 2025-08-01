package net.minestom.server.thread;

import net.minestom.server.MinecraftServer;
import net.minestom.server.Tickable;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Chunk;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread responsible for ticking {@link Chunk chunks} and {@link Entity entities}.
 * <p>
 * Created in {@link ThreadDispatcher}, and awaken every tick with a task to execute.
 */
@ApiStatus.Internal
public class TickThread extends MinestomThread {
    private final ReentrantLock lock = new ReentrantLock();
    private volatile boolean stop;

    private final AtomicReference<CountDownLatch> latchRef = new AtomicReference<>();
    private volatile long tickTimeNanos;

    private long tickNum = 0;
    final List<ThreadDispatcherImpl.Partition> entries = new ArrayList<>();

    public TickThread(int number) {
        super(MinecraftServer.THREAD_NAME_TICK + "-" + number);
    }

    public TickThread(@NotNull String name) {
        super(name);
    }

    @Override
    public void run() {
        LockSupport.park(this); // Wait for first tick
        while (!stop) {
            final CountDownLatch latch = this.latchRef.get();
            if (latch == null) {
                // Should not happen, but just in case
                LockSupport.park(this);
                continue;
            }
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                tick();
            } catch (Exception e) {
                MinecraftServer.getExceptionManager().handleException(e);
            } finally {
                lock.unlock();
                // #acquire() callbacks
            }
            this.latchRef.set(null);
            latch.countDown();
            LockSupport.park(this);
        }
    }

    protected void tick() {
        final ReentrantLock lock = this.lock;
        final long tickTime = TimeUnit.NANOSECONDS.toMillis(this.tickTimeNanos);
        for (ThreadDispatcherImpl.Partition entry : entries) {
            assert entry.thread() == this;
            final List<Tickable> elements = entry.elements();
            if (elements.isEmpty()) continue;
            for (Tickable element : elements) {
                if (lock.hasQueuedThreads()) {
                    lock.unlock();
                    // #acquire() callbacks
                    lock.lock();
                }
                try {
                    assert assertElement(element);
                    element.tick(tickTime);
                } catch (Throwable e) {
                    MinecraftServer.getExceptionManager().handleException(e);
                }
            }
        }
    }

    private boolean assertElement(Tickable element) {
        return !(element instanceof AcquirableSource<?> source)
                || source.acquirable().assignedThread() == this &&
                source.acquirable().assignedThread().lock().isHeldByCurrentThread();
    }

    void startTick(CountDownLatch latch, long tickTimeNanos) {
        CountDownLatch update = latchRef
                .updateAndGet(prevLatch -> prevLatch == null || prevLatch.getCount() == 0 ? latch : prevLatch);
        if (update != latch) {
            // Tick already in progress, wait for it to complete then start our own tick
            try {
                update.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            startTick(latch, tickTimeNanos);
            return;
        }
        if (stop || entries.isEmpty()) {
            // Nothing to tick
            latch.countDown();
            return;
        }
        this.tickTimeNanos = tickTimeNanos;
        this.tickNum++;
        LockSupport.unpark(this);
    }

    /**
     * Gets the lock used to ensure the safety of entity acquisition.
     *
     * @return the thread lock
     */
    public @NotNull ReentrantLock lock() {
        return lock;
    }

    public long getTick() {
        return tickNum;
    }

    void shutdown() {
        this.stop = true;
        LockSupport.unpark(this);
    }
}
