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

    static <T> @NotNull Acquirable<T> of(@NotNull T value) {
        return new AcquirableImpl<>(value);
    }

    default void sync(@NotNull Consumer<T> consumer) {
        final Thread currentThread = Thread.currentThread();
        final TickThread tickThread = getHandler().getTickThread();
        Acquisition.acquire(currentThread, tickThread, () -> consumer.accept(unwrap()));
    }

    default void async(@NotNull Consumer<T> consumer) {
        // TODO per-thread list
        AsyncUtils.runAsync(() -> sync(consumer));
    }

    default @NotNull Optional<T> optional() {
        final Thread currentThread = Thread.currentThread();
        final TickThread tickThread = getHandler().getTickThread();
        if (Objects.equals(currentThread, tickThread)) {
            return Optional.of(unwrap());
        }
        return Optional.empty();
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
