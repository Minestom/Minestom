package net.minestom.server.lock;

import net.minestom.server.thread.BatchThread;
import net.minestom.server.thread.batch.BatchInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.Phaser;
import java.util.function.Consumer;

/**
 * Represents an element which can be acquired.
 * Used for synchronization purpose.
 * <p>
 * Implementations of this class are recommended to be immutable (or at least thread-safe).
 * The default one is {@link AcquirableImpl}.
 *
 * @param <T> the acquirable object type
 */
public interface Acquirable<T> {

    /**
     * Blocks the current thread until 'this' can be acquired,
     * and execute {@code consumer} as a callback with the acquired object.
     *
     * @param consumer the consumer of the acquired object
     * @return true if the acquisition happened without synchonization, false otherwise
     */
    default boolean acquire(@NotNull Consumer<T> consumer) {
        final Thread currentThread = Thread.currentThread();
        Acquisition.AcquisitionData data = new Acquisition.AcquisitionData();

        final Handler handler = getHandler();
        final BatchInfo batchInfo = handler.getBatchInfo();
        final BatchThread elementThread = batchInfo != null ? batchInfo.getBatchThread() : null;

        final boolean sameThread = Acquisition.acquire(currentThread, elementThread, data);

        final T unwrap = unwrap();
        if (sameThread) {
            consumer.accept(unwrap);
        } else {
            synchronized (unwrap) {
                consumer.accept(unwrap);
            }

            // Remove the previously acquired thread from the local list
            List<Thread> acquiredThreads = data.getAcquiredThreads();
            if (acquiredThreads != null) {
                acquiredThreads.remove(elementThread);
            }

            // Notify the end of the task if required
            Phaser phaser = data.getPhaser();
            if (phaser != null) {
                phaser.arriveAndDeregister();
            }
        }

        return sameThread;
    }

    /**
     * Signals the acquisition manager to acquire 'this' at the end of the thread tick.
     * <p>
     * Thread-safety is guaranteed but not the order.
     *
     * @param consumer the consumer of the acquired object
     */
    default void scheduledAcquire(@NotNull Consumer<T> consumer) {
        Acquisition.scheduledAcquireRequest(this, consumer);
    }

    @NotNull
    T unwrap();

    @NotNull
    Handler getHandler();

    class Handler {

        private volatile BatchInfo batchInfo;

        @Nullable
        public BatchInfo getBatchInfo() {
            return batchInfo;
        }

        public void refreshBatchInfo(@NotNull BatchInfo batchInfo) {
            this.batchInfo = batchInfo;
        }

        /**
         * Executed during this element tick to empty the current thread acquisition queue.
         */
        public void acquisitionTick() {
            final BatchThread batchThread = batchInfo.getBatchThread();
            if (batchThread == null)
                return;

            Acquisition.processQueue(batchThread.getQueue());
        }
    }

}