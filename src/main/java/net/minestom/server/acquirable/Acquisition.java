package net.minestom.server.acquirable;

import net.minestom.server.thread.TickThread;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public final class Acquisition {

    /**
     * Global lock used for synchronization.
     */
    private static final ReentrantLock GLOBAL_LOCK = new ReentrantLock();

    private static final AtomicLong WAIT_COUNTER_NANO = new AtomicLong();

    /**
     * Acquires a {@link Collection}.
     * <p>
     * Order is not guaranteed.
     *
     * @param collection the collection to acquire
     * @param consumer   the consumer called for each of the collection element
     */
    protected static <T> void acquireForEach(@NotNull Collection<Acquirable<T>> collection,
                                             @NotNull Consumer<T> consumer) {
        final Thread currentThread = Thread.currentThread();
        var threadEntitiesMap = retrieveOptionalThreadMap(collection, currentThread, consumer);

        // Acquire all the threads one by one
        {
            for (var entry : threadEntitiesMap.entrySet()) {
                final TickThread tickThread = entry.getKey();
                final List<T> values = entry.getValue();

                acquire(currentThread, tickThread, () -> {
                    for (T value : values) {
                        consumer.accept(value);
                    }
                });
            }
        }
    }

    /**
     * Ensures that {@code callback} is safely executed inside the batch thread.
     */
    protected static void acquire(@NotNull Thread currentThread, @Nullable TickThread elementThread,
                                  @NotNull Runnable callback) {
        if (Objects.equals(currentThread, elementThread)) {
            callback.run();
        } else {
            var lock = acquireEnter(currentThread, elementThread);
            callback.run();
            acquireLeave(lock);
        }
    }

    protected static ReentrantLock acquireEnter(Thread currentThread, TickThread elementThread) {
        // Monitoring
        long time = System.nanoTime();

        ReentrantLock currentLock;
        {
            final TickThread current = currentThread instanceof TickThread ?
                    (TickThread) currentThread : null;
            currentLock = current != null && current.getLock().isHeldByCurrentThread() ?
                    current.getLock() : null;
        }
        if (currentLock != null)
            currentLock.unlock();

        GLOBAL_LOCK.lock();

        if (currentLock != null)
            currentLock.lock();

        final var lock = elementThread != null ? elementThread.getLock() : null;
        final boolean acquired = lock == null || lock.isHeldByCurrentThread();
        if (!acquired) {
            lock.lock();
        }

        // Monitoring
        WAIT_COUNTER_NANO.addAndGet(System.nanoTime() - time);

        return !acquired ? lock : null;
    }

    protected static void acquireLeave(ReentrantLock lock) {
        if (lock != null) {
            lock.unlock();
        }
        GLOBAL_LOCK.unlock();
    }

    /**
     * Creates
     *
     * @param collection    the acquirable collection
     * @param currentThread the current thread
     * @param consumer      the consumer to execute when an element is already in the current thread
     * @return a new Thread to acquirable elements map
     */
    protected static <T> Map<TickThread, List<T>> retrieveOptionalThreadMap(@NotNull Collection<Acquirable<T>> collection,
                                                                            @NotNull Thread currentThread,
                                                                            @NotNull Consumer<T> consumer) {
        // Separate a collection of acquirable elements into a map of thread->elements
        // Useful to reduce the number of acquisition

        Map<TickThread, List<T>> threadCacheMap = new HashMap<>();
        for (var element : collection) {
            final T value = element.unwrap();

            final TickThread elementThread = element.getHandler().getTickThread();
            if (currentThread == elementThread) {
                // The element is managed in the current thread, consumer can be immediately called
                consumer.accept(value);
            } else {
                // The element is manager in a different thread, cache it
                List<T> threadCacheList = threadCacheMap.computeIfAbsent(elementThread, tickThread -> new ArrayList<>());
                threadCacheList.add(value);
            }
        }

        return threadCacheMap;
    }

    public static long getCurrentWaitMonitoring() {
        return WAIT_COUNTER_NANO.get();
    }

    public static void resetWaitMonitoring() {
        WAIT_COUNTER_NANO.set(0);
    }
}
