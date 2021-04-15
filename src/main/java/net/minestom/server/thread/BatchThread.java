package net.minestom.server.thread;

import net.minestom.server.MinecraftServer;
import net.minestom.server.lock.Acquisition;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

public class BatchThread extends Thread {

    private final BatchRunnable runnable;

    private final BatchQueue queue;

    public BatchThread(@NotNull BatchRunnable runnable, int number) {
        super(runnable, MinecraftServer.THREAD_NAME_TICK + "-" + number);
        this.runnable = runnable;
        this.queue = new BatchQueue();

        this.runnable.setLinkedThread(this);
    }

    @NotNull
    public BatchRunnable getMainRunnable() {
        return runnable;
    }

    @NotNull
    public BatchQueue getQueue() {
        return queue;
    }

    public void shutdown() {
        this.runnable.stop = true;
    }

    public static class BatchRunnable implements Runnable {

        private volatile boolean stop;
        private BatchThread batchThread;

        private volatile boolean inTick;
        private volatile CountDownLatch countDownLatch;

        private final Queue<Runnable> queue = new ConcurrentLinkedQueue<>();

        private final Object tickLock = new Object();

        @Override
        public void run() {
            Check.notNull(batchThread, "The linked BatchThread cannot be null!");
            while (!stop) {

                // The latch is necessary to control the tick rates
                if (countDownLatch == null)
                    continue;

                synchronized (tickLock) {
                    this.inTick = true;

                    // Execute all pending runnable
                    Runnable runnable;
                    while ((runnable = queue.poll()) != null) {
                        runnable.run();
                    }

                    // Execute waiting acquisition
                    {
                        Acquisition.processThreadTick(batchThread.getQueue());
                    }

                    this.countDownLatch.countDown();
                    this.countDownLatch = null;

                    this.inTick = false;

                    // Wait for the next notify (game tick)
                    try {
                        this.tickLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public synchronized void startTick(@NotNull CountDownLatch countDownLatch, @NotNull Runnable runnable) {
            this.countDownLatch = countDownLatch;
            this.queue.add(runnable);
            synchronized (tickLock) {
                this.tickLock.notify();
            }
        }

        public boolean isInTick() {
            return inTick;
        }

        private void setLinkedThread(BatchThread batchThread) {
            this.batchThread = batchThread;
        }
    }

}