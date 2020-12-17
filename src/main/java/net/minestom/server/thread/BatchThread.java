package net.minestom.server.thread;

import com.google.common.collect.Queues;
import net.minestom.server.lock.AcquirableElement;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.UUID;

public class BatchThread extends Thread {

    private final BatchRunnable runnable;
    private final UUID identifier;

    private final Queue<Object> waitingAcquisitionQueue = Queues.newConcurrentLinkedQueue();

    private int cost;

    public BatchThread(@NotNull BatchRunnable runnable, int number) {
        super(runnable, "tick-thread-" + number);
        this.runnable = runnable;
        this.identifier = UUID.randomUUID();

        this.runnable.setLinkedThread(this);
    }

    public int getCost() {
        return cost;
    }

    @NotNull
    public BatchRunnable getMainRunnable() {
        return runnable;
    }

    @NotNull
    public UUID getIdentifier() {
        return identifier;
    }

    @NotNull
    public Queue<Object> getWaitingAcquisitionQueue() {
        return waitingAcquisitionQueue;
    }

    public void addRunnable(@NotNull Runnable runnable, int cost) {
        this.runnable.queue.add(runnable);
        this.cost += cost;
    }

    public void shutdown() {
        this.runnable.stop = true;
    }

    public static class BatchRunnable implements Runnable {

        private volatile boolean stop;
        private BatchThread batchThread;

        private final Queue<Runnable> queue = new ArrayDeque<>();

        @Override
        public void run() {
            Check.notNull(batchThread, "The linked BatchThread cannot be null!");
            while (!stop) {
                // Execute all pending runnable
                Runnable runnable;
                while ((runnable = queue.poll()) != null) {
                    runnable.run();
                }

                batchThread.cost = 0;

                // Execute waiting acquisition
                AcquirableElement.Handler.processQueue(batchThread.getWaitingAcquisitionQueue());

                // Wait for the next notify (game tick)
                try {
                    synchronized (this) {
                        wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void setLinkedThread(BatchThread batchThread) {
            this.batchThread = batchThread;
        }
    }

}
