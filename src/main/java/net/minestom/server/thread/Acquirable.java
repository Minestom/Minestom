package net.minestom.server.thread;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
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
    static Stream<Entity> localEntities() {
        if (!(Thread.currentThread() instanceof TickThread tickThread)) return Stream.empty();
        return tickThread.entries.stream()
                .flatMap(partitionEntry -> partitionEntry.elements().stream())
                .filter(tickable -> tickable instanceof Entity)
                .map(tickable -> (Entity) tickable);
    }

    /**
     * Retrieve and reset acquiring time.
     */
    @ApiStatus.Internal
    static long resetAcquiringTime() {
        return AcquirableImpl.WAIT_COUNTER_NANO.getAndSet(0);
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
    static <T> Acquirable<T> unassigned(T value) {
        return new AcquirableImpl<>(value);
    }

    /**
     * Returns a new {@link Acquired} object which will be locked to the current thread.
     * <p>
     * Useful when your code cannot be done inside a callback and need to be sync.
     * Do not forget to call {@link Acquired#unlock()} once you are done with it.
     *
     * @return an acquired object
     * @throws IllegalStateException if the acquirable element is not initialized
     * @see #sync(Consumer) for auto-closeable capability
     * @see #applySync(Function) for auto-closeable capability
     */
    Acquired<T> lock();

    /**
     * Retrieves the acquirable value if and only if the element
     * is already present/ticked in the current thread.
     * <p>
     * Useful when you want only want to acquire an element when you are guaranteed
     * to do not access any external thread.
     *
     * @return an optional containing the acquired element if safe
     * {@link Optional#empty()} otherwise
     */
    default Optional<T> local() {
        return isLocal() ? Optional.of(unwrap()) : Optional.empty();
    }

    /**
     * Gets if the acquirable element is local to this thread
     *
     * @return true if the element is linked to the current thread
     */
    boolean isLocal();

    /**
     * Retrieves the acquirable value if and only if the element
     * is already acquired/owned.
     * <p>
     * Useful when you want only want to acquire an element without depending
     * on any more lock.
     * <p>
     * Less strict than {@link #local()} as using an owned element may create contention.
     *
     * @return an optional containing the acquired element if safe
     * {@link Optional#empty()} otherwise
     */
    default Optional<T> owned() {
        return isOwned() ? Optional.of(unwrap()) : Optional.empty();
    }

    /**
     * Gets if the acquirable element is owned by this thread.
     * Either by being local, or by already being acquired in the current scope.
     *
     * @return true if the element is linked to the current thread
     */
    boolean isOwned();

    /**
     * Locks the acquirable element, execute {@code consumer} synchronously and unlock the thread.
     * <p>
     * Free if the element is already present in the current thread, blocking otherwise.
     *
     * @param consumer the callback to execute once the element has been safely acquired
     */
    void sync(Consumer<T> consumer);

    /**
     * Try to cheaply lock the acquirable element, execute {@code consumer} synchronously and unlock the thread.
     * <p>
     * Returns false if there is contention.
     *
     * @param consumer the callback to execute once the element has been safely acquired
     * @return true if the consumer was executed, false otherwise
     */
    boolean trySync(Consumer<T> consumer);

    /**
     * Locks the acquirable element, execute {@code function} synchronously and unlock the thread.
     * <p>
     * Free if the element is already present in the current thread, blocking otherwise.
     *
     * @param function the function to execute once the element has been safely acquired
     */
    default <R> R applySync(Function<T, R> function) {
        Acquired<T> acquired = lock();
        try {
            return function.apply(acquired.get());
        } finally {
            acquired.unlock();
        }
    }


    /**
     * Unwrap the contained object unsafely.
     * <p>
     * Should only be considered when thread-safety is not necessary (e.g. comparing positions)
     *
     * @return the unwrapped value
     */
    T unwrap();

    /**
     * Gets the thread to which this acquirable element is assigned.
     * <p>
     * May change to one tick to the next.
     *
     * @return the assigned thread, null if not initialized (likely on the next tick)
     */
    @UnknownNullability
    TickThread assignedThread();

    /**
     * Checks if the current thread owns the acquirable element.
     * <p>
     * Throws an {@link AcquirableOwnershipException} if not owned.
     * <p>
     * This method is only enabled when assertions are enabled or
     * {@link net.minestom.server.ServerFlag#ACQUIRABLE_STRICT} is set to true.
     *
     * @throws AcquirableOwnershipException if the current thread does not own the acquirable element
     */
    void assertOwnership();
}
