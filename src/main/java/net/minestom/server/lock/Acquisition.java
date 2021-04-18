package net.minestom.server.lock;

import com.google.common.util.concurrent.Monitor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.thread.BatchThread;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public final class Acquisition {

    private static final ThreadLocal<ScheduledAcquisition> SCHEDULED_ACQUISITION = ThreadLocal.withInitial(ScheduledAcquisition::new);

    /**
     * Global lock used for synchronization.
     */
    private static final Monitor GLOBAL_MONITOR = new Monitor();

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
        Map<BatchThread, List<E>> threadCacheMap = retrieveThreadMap(collection, currentThread, consumer);

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

            // Monitoring
            final boolean monitoring = MinecraftServer.hasWaitMonitoring();
            long time = 0;
            if (monitoring) {
                time = System.nanoTime();
            }

            BatchThread current = (BatchThread) currentThread;
            Monitor currentMonitor = current.monitor;
            final boolean currentAcquired = currentMonitor.isOccupiedByCurrentThread();
            if (currentAcquired)
                current.monitor.leave();

            GLOBAL_MONITOR.enter();

            if (currentAcquired)
                current.monitor.enter();

            final var monitor = elementThread != null ? elementThread.monitor : null;
            final boolean acquired = monitor == null || monitor.isOccupiedByCurrentThread();
            if (!acquired) {
                monitor.enter();
            }

            // Monitoring
            if (monitoring) {
                time = System.nanoTime() - time;
                WAIT_COUNTER_NANO.addAndGet(time);
            }

            callback.run();

            if (!acquired) {
                monitor.leave();
            }
            GLOBAL_MONITOR.leave();
        }
    }

    protected synchronized static <T> void scheduledAcquireRequest(@NotNull Acquirable<T> acquirable, Consumer<T> consumer) {
        ScheduledAcquisition scheduledAcquisition = SCHEDULED_ACQUISITION.get();
        scheduledAcquisition.acquirableElements.add((Acquirable<Object>) acquirable);
        scheduledAcquisition.callbacks
                .computeIfAbsent(acquirable.unwrap(), objectAcquirable -> new ArrayList<>())
                .add((Consumer<Object>) consumer);
    }

    private static <E, T extends Acquirable<E>> Map<BatchThread, List<E>> retrieveThreadMap(@NotNull Collection<? super T> collection,
                                                                                            @NotNull Thread currentThread,
                                                                                            @NotNull Consumer<? super E> consumer) {
        // Separate a collection of acquirable elements into a map of thread->elements
        // Useful to reduce the number of acquisition

        Map<BatchThread, List<E>> threadCacheMap = new HashMap<>();
        for (Object obj : collection) {
            T element = (T) obj;
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