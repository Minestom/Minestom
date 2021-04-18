package net.minestom.server.lock;

import net.minestom.server.entity.Entity;
import net.minestom.server.thread.BatchThread;
import net.minestom.server.thread.ThreadProvider;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
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
     */
    default void acquire(@NotNull Consumer<@NotNull T> consumer) {
        final Thread currentThread = Thread.currentThread();
        final BatchThread elementThread = getHandler().getBatchThread();
        Acquisition.acquire(currentThread, elementThread, () -> consumer.accept(unwrap()));
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

        private volatile ThreadProvider.ChunkEntry chunkEntry;

        public ThreadProvider.ChunkEntry getChunkEntry() {
            return chunkEntry;
        }

        @ApiStatus.Internal
        public void refreshChunkEntry(@NotNull ThreadProvider.ChunkEntry chunkEntry) {
            this.chunkEntry = chunkEntry;
        }

        public BatchThread getBatchThread() {
            return chunkEntry != null ? chunkEntry.getThread() : null;
        }
    }

}