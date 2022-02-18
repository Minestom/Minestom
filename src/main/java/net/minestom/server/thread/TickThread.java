package net.minestom.server.thread;

import net.minestom.server.MinecraftServer;
import net.minestom.server.Tickable;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Chunk;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread responsible for ticking {@link Chunk chunks} and {@link Entity entities}.
 * <p>
 * Created in {@link ThreadDispatcher}, and awaken every tick with a task to execute.
 */
@ApiStatus.Internal
public final class TickThread extends MinestomThread {
    private final ReentrantLock lock = new ReentrantLock();
    private volatile boolean stop;

    private CountDownLatch latch;
    private long tickTime;
    private final List<ThreadDispatcher.Partition> entries = new ArrayList<>();

    public TickThread(int number) {
        super(MinecraftServer.THREAD_NAME_TICK + "-" + number);
    }

    @Override
    public void run() {
        LockSupport.park(this);
        while (!stop) {
            this.lock.lock();
            try {
                tick();
            } catch (Exception e) {
                MinecraftServer.getExceptionManager().handleException(e);
            }
            this.lock.unlock();
            // #acquire() callbacks
            this.latch.countDown();
            LockSupport.park(this);
        }
    }

    private void tick() {
        for (ThreadDispatcher.Partition entry : entries) {
            final List<Tickable> elements = entry.elements();
            if (elements.isEmpty()) continue;
            for (Tickable element : elements) {
                if (lock.hasQueuedThreads()) {
                    this.lock.unlock();
                    // #acquire() callbacks should be called here
                    this.lock.lock();
                }
                try {
                    element.tick(tickTime);
                } catch (Throwable e) {
                    MinecraftServer.getExceptionManager().handleException(e);
                }
            }
        }
    }

    void startTick(CountDownLatch latch, long tickTime) {
        if (entries.isEmpty()) {
            // Nothing to tick
            latch.countDown();
            return;
        }
        this.latch = latch;
        this.tickTime = tickTime;
        this.stop = false;
        LockSupport.unpark(this);
    }

    public Collection<ThreadDispatcher.Partition> entries() {
        return entries;
    }

    /**
     * Gets the lock used to ensure the safety of entity acquisition.
     *
     * @return the thread lock
     */
    public @NotNull ReentrantLock lock() {
        return lock;
    }

    void shutdown() {
        this.stop = true;
        LockSupport.unpark(this);
    }
}
