package net.minestom.server.thread;

import net.minestom.server.Tickable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.function.IntFunction;

/**
 * ThreadDispatcher can be used to dispatch updates (ticks) across a number of "partitions" (such as chunks) that
 * house {@link Tickable} instances (such as entities). The parallelism of such updates is defined when the dispatcher
 * is constructed.
 * <p>
 * It is recommended that {@link Tickable}s being added to a dispatcher also implement {@link AcquirableSource}, as
 * doing so will allow the user to synchronize external access to them using the {@link Acquirable} API.
 * <p>
 * Instances of this class can be obtained by calling {@link ThreadDispatcher#dispatcher(ThreadProvider, int)}, or a similar
 * overload.
 *
 * @see Acquirable
 * @see AcquirableSource
 */
public sealed interface ThreadDispatcher<P> permits ThreadDispatcherImpl {
    /**
     * Creates a new ThreadDispatcher using default thread names (ex. Ms-Tick-n).
     * <p>Remember to start the dispatcher using {@link #start()}</p>
     *
     * @param provider    the {@link ThreadProvider} instance to be used for defining thread IDs
     * @param threadCount the number of threads to create for this dispatcher
     * @param <P>         the dispatcher partition type
     * @return a new ThreadDispatcher instance
     */
    @Contract(pure = true)
    static <P> @NotNull ThreadDispatcher<P> dispatcher(@NotNull ThreadProvider<P> provider, int threadCount) {
        return new ThreadDispatcherImpl<>(provider, threadCount, TickThread::new);
    }

    /**
     * Creates a new ThreadDispatcher using the caller-provided thread name generator {@code nameGenerator}. This is
     * useful to disambiguate custom ThreadDispatcher instances from ones used in core Minestom code.
     * <p>Remember to start the dispatcher using {@link #start()}</p>
     *
     * @param provider      the {@link ThreadProvider} instance to be used for defining thread IDs
     * @param nameGenerator a function that should return unique names, given a thread index
     * @param threadCount   the number of threads to create for this dispatcher
     * @param <P>           the dispatcher partition type
     * @return a new ThreadDispatcher instance
     */
    @Contract(pure = true)
    static <P> @NotNull ThreadDispatcher<P> dispatcher(@NotNull ThreadProvider<P> provider,
                                                       @NotNull IntFunction<String> nameGenerator, int threadCount) {
        return new ThreadDispatcherImpl<>(provider, threadCount, index -> new TickThread(nameGenerator.apply(index)));
    }

    /**
     * Creates a single-threaded dispatcher that uses default thread names.
     * <p>Remember to start the dispatcher using {@link #start()}</p>
     *
     * @param <P> the dispatcher partition type
     * @return a new ThreadDispatcher instance
     */
    @Contract(pure = true)
    static <P> @NotNull ThreadDispatcher<P> singleThread() {
        return dispatcher(ThreadProvider.counter(), 1);
    }

    /**
     * Gets the unmodifiable list of TickThreads used to dispatch updates.
     * <p>
     * This method is marked internal to reflect {@link TickThread}s own internal status.
     *
     * @return the TickThreads used to dispatch updates
     */
    @Unmodifiable
    @ApiStatus.Internal
    @NotNull List<@NotNull TickThread> threads();

    /**
     * Prepares the update by creating the {@link TickThread} tasks.
     *
     * @param time the tick time in nanos
     */
    void updateAndAwait(long time);

    /**
     * Called at the end of each tick to clear removed tickables, refresh the partition linked to a tickable, and
     * partition threads based on {@link ThreadProvider#findThread(Object)}.
     *
     * @param nanoTimeout max time in nanoseconds to update partitions
     */
    void refreshThreads(long nanoTimeout);

    /**
     * Refreshes all thread as per {@link ThreadDispatcher#refreshThreads(long)}, with a timeout of
     * {@link Long#MAX_VALUE}.
     */
    default void refreshThreads() {
        refreshThreads(Long.MAX_VALUE);
    }

    /**
     * Signals an update to the dispatcher.
     * <p>
     * This method is used to notify the dispatcher of changes that need to be processed, such as partition loads,
     * unloads, or element updates.
     * <p>
     * Updates are processed at the start of each tick, before the actual ticking of elements.
     *
     * @param update the update to signal
     */
    void signalUpdate(@NotNull ThreadDispatcher.Update<P> update);

    default void createPartition(@NotNull P partition) {
        signalUpdate(new Update.PartitionLoad<>(partition));
    }

    default void deletePartition(@NotNull P partition) {
        signalUpdate(new Update.PartitionUnload<>(partition));
    }

    default void updateElement(@NotNull Tickable tickable, @NotNull P partition) {
        signalUpdate(new Update.ElementUpdate<>(tickable, partition));
    }

    default void removeElement(@NotNull Tickable tickable) {
        signalUpdate(new Update.ElementRemove<>(tickable));
    }

    /**
     * Starts all the {@link TickThread tick threads}.
     * <p>
     * This will throw an {@link IllegalThreadStateException} if the threads have already been started.
     */
    void start();

    /**
     * Checks if all the {@link TickThread tick threads} are alive.
     *
     * @return true if all threads are alive, false otherwise
     */
    boolean isAlive();

    /**
     * Shutdowns all the {@link TickThread tick threads}.
     * <p>
     * Action is irreversible.
     */
    void shutdown();

    @ApiStatus.Internal
    @SuppressWarnings("unused")
    sealed interface Update<P> {

        /**
         * Registers a new partition.
         *
         * @param partition the partition to register
         */
        record PartitionLoad<P>(@NotNull P partition) implements Update<P> {
        }

        /**
         * Deletes an existing partition.
         *
         * @param partition the partition to delete
         */
        record PartitionUnload<P>(@NotNull P partition) implements Update<P> {
        }

        /**
         * Updates a {@link Tickable}, signalling that it is a part of {@code partition}.
         *
         * @param tickable  the Tickable to update
         * @param partition the partition the Tickable is part of
         */
        record ElementUpdate<P>(@NotNull Tickable tickable, P partition) implements Update<P> {
        }

        /**
         * Removes a {@link Tickable}.
         *
         * @param tickable the Tickable to remove
         */
        record ElementRemove<P>(@NotNull Tickable tickable) implements Update<P> {
        }
    }
}
