package net.minestom.server.entity.acquirable;

import net.minestom.server.entity.Entity;
import net.minestom.server.thread.BatchThread;
import net.minestom.server.thread.ThreadProvider;
import net.minestom.server.utils.consumer.EntityConsumer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Represents an element which can be acquired.
 * Used for synchronization purpose.
 */
public class AcquirableEntity {

    public static final ThreadLocal<Collection<Entity>> CURRENT_ENTITIES = ThreadLocal.withInitial(Collections::emptyList);

    /**
     * Gets all the {@link Entity entities} being ticked in the current thread.
     * <p>
     * Useful when you want to ensure that no acquisition is ever done.
     *
     * @return the entities ticked in the current thread
     */
    public static @NotNull Collection<@NotNull Entity> current() {
        return CURRENT_ENTITIES.get();
    }

    /**
     * Changes the collection returned by {@link #current()}.
     * <p>
     * Mostly for internal use, internal calls are unrecommended as they could lead
     * to unexpected behavior.
     *
     * @param entities the new entity collection
     */
    @ApiStatus.Internal
    public static void refresh(@NotNull Collection<@NotNull Entity> entities) {
        CURRENT_ENTITIES.set(entities);
    }

    private final Entity entity;
    private final Handler handler;

    public AcquirableEntity(@NotNull Entity entity) {
        this.entity = entity;
        this.handler = new Handler();
    }

    /**
     * Blocks the current thread until 'this' can be acquired,
     * and execute {@code consumer} as a callback with the acquired object.
     *
     * @param consumer the acquisition consumer
     */
    public void acquire(@NotNull EntityConsumer consumer) {
        final Thread currentThread = Thread.currentThread();
        final BatchThread elementThread = getHandler().getBatchThread();
        Acquisition.acquire(currentThread, elementThread, () -> consumer.accept(unwrap()));
    }

    /**
     * Executes {@code consumer} only if this element can be safely
     * acquired without any synchronization.
     *
     * @param consumer the acquisition consumer
     * @return true if the acquisition happened without synchronization,
     * false otherwise
     */
    public boolean tryAcquire(@NotNull EntityConsumer consumer) {
        final Thread currentThread = Thread.currentThread();
        final BatchThread elementThread = getHandler().getBatchThread();
        if (Objects.equals(currentThread, elementThread)) {
            consumer.accept(unwrap());
            return true;
        }
        return false;
    }

    /**
     * Retrieves {@link #unwrap()} only if this element can be safely
     * acquired without any synchronization.
     *
     * @return this element or null if unsafe
     */
    public @Nullable Entity tryAcquire() {
        final Thread currentThread = Thread.currentThread();
        final BatchThread elementThread = getHandler().getBatchThread();
        if (Objects.equals(currentThread, elementThread)) {
            return unwrap();
        }
        return null;
    }

    /**
     * Signals the acquisition manager to acquire 'this' at the end of the thread tick.
     * <p>
     * Thread-safety is guaranteed but not the order.
     *
     * @param consumer the consumer of the acquired object
     */
    public void scheduledAcquire(@NotNull EntityConsumer consumer) {
        Acquisition.scheduledAcquireRequest(this, (Consumer<Entity>) consumer);
    }

    /**
     * Unwrap the contained object unsafely.
     * <p>
     * Should only be considered when thread-safety is not necessary (e.g. comparing positions)
     *
     * @return the unwrapped value
     */
    public @NotNull Entity unwrap() {
        return entity;
    }

    /**
     * Gets the {@link Handler} of this acquirable element,
     * containing the currently linked thread.
     *
     * @return this element handler
     */
    public @NotNull Handler getHandler() {
        return handler;
    }

    public static class Handler {

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
