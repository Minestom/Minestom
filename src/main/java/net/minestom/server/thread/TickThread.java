package net.minestom.server.thread;

import net.minestom.server.MinecraftServer;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread responsible for ticking {@link net.minestom.server.instance.Chunk chunks} and {@link net.minestom.server.entity.Entity entities}.
 * <p>
 * Created in {@link ThreadProvider}, and awaken every tick with a task to execute.
 */
public class TickThread extends Thread {

    protected final BatchRunnable runnable;
    private final ReentrantLock lock = new ReentrantLock();

    public TickThread(@NotNull BatchRunnable runnable, int number) {
        super(runnable, MinecraftServer.THREAD_NAME_TICK + "-" + number);
        this.runnable = runnable;

        this.runnable.setLinkedThread(this);
    }

    /**
     * Gets the lock used to ensure the safety of entity acquisition.
     *
     * @return the thread lock
     */
    public @NotNull ReentrantLock getLock() {
        return lock;
    }

    /**
     * Shutdowns the thread. Cannot be undone.
     */
    public void shutdown() {
        this.runnable.stop = true;
        LockSupport.unpark(this);
    }

    protected static class BatchRunnable implements Runnable {

        private volatile boolean stop;
        private TickThread tickThread;

        private volatile boolean inTick;
        private final AtomicReference<CountDownLatch> countDownLatch = new AtomicReference<>();

        private final Queue<Runnable> queue = new ConcurrentLinkedQueue<>();

        @Override
        public void run() {
            Check.notNull(tickThread, "The linked BatchThread cannot be null!");
            while (!stop) {
                LockSupport.park(tickThread);
                if (stop)
                    break;
                CountDownLatch localCountDownLatch = this.countDownLatch.get();

                // The latch is necessary to control the tick rates
                if (localCountDownLatch == null) {
                    continue;
                }

                this.inTick = true;

                // Execute all pending runnable
                Runnable runnable;
                while ((runnable = queue.poll()) != null) {
                    runnable.run();
                }

                localCountDownLatch.countDown();
                this.countDownLatch.compareAndSet(localCountDownLatch, null);

                // Wait for the next notify (game tick)
                this.inTick = false;
            }
        }

        public void startTick(@NotNull CountDownLatch countDownLatch, @NotNull Runnable runnable) {
            this.countDownLatch.set(countDownLatch);
            this.queue.add(runnable);
            LockSupport.unpark(tickThread);
        }

        public boolean isInTick() {
            return inTick;
        }

        private void setLinkedThread(TickThread tickThread) {
            this.tickThread = tickThread;
        }
    }

}
