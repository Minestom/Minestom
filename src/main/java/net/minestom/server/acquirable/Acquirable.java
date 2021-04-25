package net.minestom.server.acquirable;

import net.minestom.server.entity.Entity;
import net.minestom.server.thread.ThreadProvider;
import net.minestom.server.thread.TickThread;
import net.minestom.server.utils.async.AsyncUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface Acquirable<T> {

    /**
     * Gets all the {@link Entity entities} being ticked in the current thread.
     * <p>
     * Useful when you want to ensure that no acquisition is ever done.
     *
     * @return the entities ticked in the current thread
     */
    static @NotNull Stream<@NotNull Entity> currentEntities() {
        return AcquirableImpl.CURRENT_ENTITIES.get();
    }

    /**
     * Changes the stream returned by {@link #currentEntities()}.
     * <p>
     * Mostly for internal use, external calls are unrecommended as they could lead
     * to unexpected behavior.
     *
     * @param entities the new entity stream
     */
    @ApiStatus.Internal
    static void refreshEntities(@NotNull Stream<@NotNull Entity> entities) {
        AcquirableImpl.CURRENT_ENTITIES.set(entities);
    }

    /**
     * Creates a new {@link Acquirable} object.
     * <p>
     * Mostly for internal use, as a {@link TickThread} has to be used
     * and properly synchronized.
     *
     * @param value the acquirable element
     * @param <T>   the acquirable element type
     * @return a new acquirable object
     */
    @ApiStatus.Internal
    static <T> @NotNull Acquirable<T> of(@NotNull T value) {
        return new AcquirableImpl<>(value);
    }

    /**
     * Returns a new {@link Acquired} object which will be locked to the current thread.
     * <p>
     * Useful when your code cannot be done inside a callback and need to be sync.
     * Do not forget to call {@link Acquired#unlock()} once you are done with it.
     *
     * @return an acquired object
     * @see #sync(Consumer) for auto-closeable capability
     */
    default @NotNull Acquired<T> lock() {
        var optional = local();
        if (optional.isPresent()) {
            return new Acquired<>(optional.get(), false, null);
        } else {
            final Thread currentThread = Thread.currentThread();
            final TickThread tickThread = getHandler().getTickThread();
            var lock = Acquisition.acquireEnter(currentThread, tickThread);
            return new Acquired<>(unwrap(), true, lock);
        }
    }

    /**
     * Retrieves the acquirable value if and only if the element
     * is already present/ticked in the current thread.
     * <p>
     * Useful when you want only want to acquire an element when you are guaranteed
     * to do not create a huge performance impact.
     *
     * @return an optional containing the acquired element if safe,
     * {@link Optional#empty()} otherwise
     */
    default @NotNull Optional<T> local() {
        final Thread currentThread = Thread.currentThread();
        final TickThread tickThread = getHandler().getTickThread();
        if (Objects.equals(currentThread, tickThread)) {
            return Optional.of(unwrap());
        }
        return Optional.empty();
    }

    /**
     * Locks the acquirable element, execute {@code consumer} synchronously and unlock the thread.
     * <p>
     * Free if the element is already present in the current thread, blocking otherwise.
     *
     * @param consumer the callback to execute once the element has been safely acquired
     * @see #async(Consumer)
     */
    default void sync(@NotNull Consumer<T> consumer) {
        var acquired = lock();
        consumer.accept(acquired.get());
        acquired.unlock();
    }

    /**
     * Locks the acquirable element, execute {@code consumer} asynchronously and unlock the thread.
     * <p>
     * Free if the element is already present in the current thread, blocking otherwise.
     *
     * @param consumer the callback to execute once the element has been safely acquired
     * @see #sync(Consumer)
     */
    default void async(@NotNull Consumer<T> consumer) {
        // TODO per-thread list
        AsyncUtils.runAsync(() -> sync(consumer));
    }

    /**
     * Unwrap the contained object unsafely.
     * <p>
     * Should only be considered when thread-safety is not necessary (e.g. comparing positions)
     *
     * @return the unwrapped value
     */
    @NotNull T unwrap();

    /**
     * Gets the {@link Handler} of this acquirable element,
     * containing the currently linked thread.
     * <p>
     * Mostly for internal use.
     *
     * @return this element handler
     */
    @ApiStatus.Internal
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

        public TickThread getTickThread() {
            return chunkEntry != null ? chunkEntry.getThread() : null;
        }
    }

}
