package net.minestom.server.lock;

import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Chunk;
import net.minestom.server.thread.BatchThread;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
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

    ThreadLocal<Collection<Entity>> CURRENT_ENTITIES = ThreadLocal.withInitial(Collections::emptyList);

    static @NotNull Collection<@NotNull Entity> currentEntities() {
        return CURRENT_ENTITIES.get();
    }

    @ApiStatus.Internal
    static void refreshEntities(@NotNull Collection<@NotNull Entity> entities) {
        CURRENT_ENTITIES.set(entities);
    }

    /**
     * Blocks the current thread until 'this' can be acquired,
     * and execute {@code consumer} as a callback with the acquired object.
     *
     * @param consumer the consumer of the acquired object
     * @return true if the acquisition happened without synchronization, false otherwise
     */
    default boolean acquire(@NotNull Consumer<T> consumer) {
        final Thread currentThread = Thread.currentThread();

        final Handler handler = getHandler();
        final BatchThread elementThread = handler.getBatchThread();

        Acquisition.acquire(currentThread, elementThread, () -> consumer.accept(unwrap()));

        return true;
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

    @NotNull T unwrap();

    @NotNull Handler getHandler();

    class Handler {

        private volatile BatchThread batchThread;
        private volatile Chunk batchChunk;

        public BatchThread getBatchThread() {
            return batchThread;
        }

        public Chunk getBatchChunk() {
            return batchChunk;
        }

        @ApiStatus.Internal
        public void refreshBatchInfo(@NotNull BatchThread batchThread, @NotNull Chunk batchChunk) {
            this.batchThread = batchThread;
            this.batchChunk = batchChunk;
        }

        /**
         * Executed during this element tick to empty the current thread acquisition queue.
         */
        public void acquisitionTick() {
            if (batchThread == null)
                return;
            Acquisition.process(batchThread);
        }
    }

    class Request {
        public CountDownLatch localLatch, processLatch;
    }

}