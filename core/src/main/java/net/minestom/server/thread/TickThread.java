package net.minestom.server.thread;

import net.minestom.server.MinecraftServer;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

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

        private final AtomicReference<TickContext> tickContext = new AtomicReference<>();

        @Override
        public void run() {
            Check.notNull(tickThread, "The linked BatchThread cannot be null!");
            while (!stop) {
                LockSupport.park(tickThread);
                if (stop)
                    break;
                TickContext localContext = this.tickContext.get();
                // The context is necessary to control the tick rates
                if (localContext == null) {
                    continue;
                }

                // Execute tick
                localContext.runnable.run();

                localContext.countDownLatch.countDown();
                this.tickContext.compareAndSet(localContext, null);
            }
        }

        protected void startTick(@NotNull CountDownLatch countDownLatch, @NotNull Runnable runnable) {
            this.tickContext.set(new TickContext(countDownLatch, runnable));
            LockSupport.unpark(tickThread);
        }

        private void setLinkedThread(TickThread tickThread) {
            this.tickThread = tickThread;
        }
    }

    private static class TickContext {
        private final CountDownLatch countDownLatch;
        private final Runnable runnable;

        private TickContext(@NotNull CountDownLatch countDownLatch, @NotNull Runnable runnable) {
            this.countDownLatch = countDownLatch;
            this.runnable = runnable;
        }
    }

}
