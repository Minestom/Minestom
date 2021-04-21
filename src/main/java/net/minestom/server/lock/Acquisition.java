package net.minestom.server.lock;

import net.minestom.server.MinecraftServer;
import net.minestom.server.thread.BatchThread;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public final class Acquisition {

    private static final ThreadLocal<ScheduledAcquisition> SCHEDULED_ACQUISITION = ThreadLocal.withInitial(ScheduledAcquisition::new);

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
     * @param <E>        the object type
     */
    public static <E> void acquireForEach(@NotNull Collection<Acquirable<E>> collection,
                                          @NotNull Consumer<? super E> consumer) {
        final Thread currentThread = Thread.currentThread();
        Map<BatchThread, List<E>> threadCacheMap = retrieveOptionalThreadMap(collection, currentThread, consumer);

        // Acquire all the threads one by one
        {
            for (Map.Entry<BatchThread, List<E>> entry : threadCacheMap.entrySet()) {
                final BatchThread batchThread = entry.getKey();
                final List<E> elements = entry.getValue();

                acquire(currentThread, batchThread, () -> {
                    for (E element : elements) {
                        consumer.accept(element);
                    }
                });
            }
        }
    }

    /**
     * Processes all scheduled acquisitions.
     */
    public static void processThreadTick() {
        ScheduledAcquisition scheduledAcquisition = SCHEDULED_ACQUISITION.get();

        final List<Acquirable<Object>> acquirableElements = scheduledAcquisition.acquirableElements;

        if (!acquirableElements.isEmpty()) {
            final Map<Object, List<Consumer<Object>>> callbacks = scheduledAcquisition.callbacks;

            acquireForEach(acquirableElements, element -> {
                List<Consumer<Object>> consumers = callbacks.get(element);
                if (consumers == null || consumers.isEmpty())
                    return;
                consumers.forEach(objectConsumer -> objectConsumer.accept(element));
            });

            // Clear collections..
            acquirableElements.clear();
            callbacks.clear();
        }
    }

    /**
     * Ensures that {@code callback} is safely executed inside the batch thread.
     */
    protected static void acquire(@NotNull Thread currentThread, @Nullable BatchThread elementThread,
                                  @NotNull Runnable callback) {
        if (Objects.equals(currentThread, elementThread)) {
            callback.run();
        } else {
            var lock = acquireEnter(currentThread, elementThread);
            callback.run();
            acquireLeave(lock);
        }
    }

    protected static ReentrantLock acquireEnter(Thread currentThread, BatchThread elementThread) {
        // Monitoring
        final boolean monitoring = MinecraftServer.hasWaitMonitoring();
        long time = 0;
        if (monitoring) {
            time = System.nanoTime();
        }

        ReentrantLock currentLock;
        {
            final BatchThread current = currentThread instanceof BatchThread ?
                    (BatchThread) currentThread : null;
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
        if (monitoring) {
            time = System.nanoTime() - time;
            WAIT_COUNTER_NANO.addAndGet(time);
        }

        return !acquired ? lock : null;
    }

    protected static ReentrantLock acquireEnter(BatchThread elementThread) {
        return acquireEnter(Thread.currentThread(), elementThread);
    }

    protected static void acquireLeave(ReentrantLock lock) {
        if (lock != null) {
            lock.unlock();
        }
        GLOBAL_LOCK.unlock();
    }

    protected synchronized static <T> void scheduledAcquireRequest(@NotNull Acquirable<T> acquirable, Consumer<T> consumer) {
        ScheduledAcquisition scheduledAcquisition = SCHEDULED_ACQUISITION.get();
        scheduledAcquisition.acquirableElements.add((Acquirable<Object>) acquirable);
        scheduledAcquisition.callbacks
                .computeIfAbsent(acquirable.unwrap(), objectAcquirable -> new ArrayList<>())
                .add((Consumer<Object>) consumer);
    }

    /**
     * Creates
     *
     * @param collection    the acquirable collection
     * @param currentThread the current thread
     * @param consumer      the consumer to execute when an element is already in the current thread
     * @param <E>           the acquirable element type
     * @return a new Thread to acquirable elements map
     */
    protected static <E> Map<BatchThread, List<E>> retrieveOptionalThreadMap(@NotNull Collection<Acquirable<E>> collection,
                                                                             @NotNull Thread currentThread,
                                                                             @NotNull Consumer<? super E> consumer) {
        // Separate a collection of acquirable elements into a map of thread->elements
        // Useful to reduce the number of acquisition

        Map<BatchThread, List<E>> threadCacheMap = new HashMap<>();
        for (Acquirable<E> element : collection) {
            final E value = element.unwrap();

            final BatchThread elementThread = element.getHandler().getBatchThread();
            if (currentThread == elementThread) {
                // The element is managed in the current thread, consumer can be immediately called
                consumer.accept(value);
            } else {
                // The element is manager in a different thread, cache it
                List<E> threadCacheList = threadCacheMap.computeIfAbsent(elementThread, batchThread -> new ArrayList<>());
                threadCacheList.add(value);
            }
        }

        return threadCacheMap;
    }

    protected static <E> Map<BatchThread, List<E>> retrieveThreadMap(@NotNull Collection<Acquirable<E>> collection) {
        // Separate a collection of acquirable elements into a map of thread->elements
        // Useful to reduce the number of acquisition
        Map<BatchThread, List<E>> threadCacheMap = new HashMap<>();
        for (Acquirable<E> element : collection) {
            final E value = element.unwrap();
            final BatchThread elementThread = element.getHandler().getBatchThread();

            List<E> threadCacheList = threadCacheMap.computeIfAbsent(elementThread, batchThread -> new ArrayList<>());
            threadCacheList.add(value);
        }

        return threadCacheMap;
    }

    public static long getCurrentWaitMonitoring() {
        return WAIT_COUNTER_NANO.get();
    }

    public static void resetWaitMonitoring() {
        WAIT_COUNTER_NANO.set(0);
    }

    private static class ScheduledAcquisition {
        private final List<Acquirable<Object>> acquirableElements = new ArrayList<>();
        private final Map<Object, List<Consumer<Object>>> callbacks = new HashMap<>();
    }
}