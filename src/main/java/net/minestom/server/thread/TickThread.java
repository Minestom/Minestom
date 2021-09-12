package net.minestom.server.thread;

import net.minestom.server.MinecraftServer;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread responsible for ticking {@link net.minestom.server.instance.Chunk chunks} and {@link net.minestom.server.entity.Entity entities}.
 * <p>
 * Created in {@link ThreadDispatcher}, and awaken every tick with a task to execute.
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
        private static final AtomicReferenceFieldUpdater<BatchRunnable, TickContext> CONTEXT_UPDATER =
                AtomicReferenceFieldUpdater.newUpdater(BatchRunnable.class, TickContext.class, "tickContext");

        private volatile boolean stop;
        private TickThread tickThread;

        private volatile TickContext tickContext;

        @Override
        public void run() {
            Check.notNull(tickThread, "The linked BatchThread cannot be null!");
            while (!stop) {
                final TickContext localContext = tickContext;
                // The context is necessary to control the tick rates
                if (localContext != null) {
                    // Execute tick
                    CONTEXT_UPDATER.compareAndSet(this, localContext, null);
                    localContext.runnable.run();
                    localContext.phaser.arriveAndDeregister();
                }
                LockSupport.park(this);
            }
        }

        protected void startTick(@NotNull Phaser phaser, @NotNull Runnable runnable) {
            this.tickContext = new TickContext(phaser, runnable);
            LockSupport.unpark(tickThread);
        }

        private void setLinkedThread(TickThread tickThread) {
            this.tickThread = tickThread;
        }
    }

    private static class TickContext {
        private final Phaser phaser;
        private final Runnable runnable;

        private TickContext(@NotNull Phaser phaser, @NotNull Runnable runnable) {
            this.phaser = phaser;
            this.runnable = runnable;
        }
    }
}
