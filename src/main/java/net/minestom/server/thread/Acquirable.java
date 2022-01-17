package net.minestom.server.thread;

import net.minestom.server.entity.Entity;
import net.minestom.server.utils.async.AsyncUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

@ApiStatus.Experimental
public sealed interface Acquirable<T> permits AcquirableImpl {

    /**
     * Gets all the {@link Entity entities} being ticked in the current thread.
     * <p>
     * Useful when you want to ensure that no acquisition is ever done.
     * <p>
     * Be aware that the entity stream is only updated at the beginning of the thread tick.
     *
     * @return the entities ticked in the current thread
     */
    static @NotNull Stream<@NotNull Entity> currentEntities() {
        final Thread currentThread = Thread.currentThread();
        if (currentThread instanceof TickThread) {
            return ((TickThread) currentThread).entries().stream()
                    .flatMap(partitionEntry -> partitionEntry.elements().stream())
                    .filter(tickable -> tickable instanceof Entity)
                    .map(tickable -> (Entity) tickable);
        }
        return Stream.empty();
    }

    /**
     * Gets the time spent acquiring since last tick.
     *
     * @return the acquiring time
     */
    static long getAcquiringTime() {
        return AcquirableImpl.WAIT_COUNTER_NANO.get();
    }

    /**
     * Resets {@link #getAcquiringTime()}.
     * <p>
     * Mostly for internal use.
     */
    @ApiStatus.Internal
    static void resetAcquiringTime() {
        AcquirableImpl.WAIT_COUNTER_NANO.set(0);
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
        return new Acquired<>(unwrap(), assignedThread());
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
        if (isLocal()) return Optional.of(unwrap());
        return Optional.empty();
    }

    /**
     * Gets if the acquirable element is local to this thread
     *
     * @return true if the element is linked to the current thread
     */
    default boolean isLocal() {
        return Thread.currentThread() == assignedThread();
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
        Acquired<T> acquired = lock();
        consumer.accept(acquired.get());
        acquired.unlock();
    }

    /**
     * Async version of {@link #sync(Consumer)}.
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

    @ApiStatus.Internal
    @NotNull TickThread assignedThread();
}
