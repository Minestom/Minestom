package net.minestom.server.lock;

import com.google.common.util.concurrent.Monitor;
import net.minestom.server.thread.BatchThread;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public final class Acquisition {

    private static final Map<BatchThread, Collection<Acquirable.Request>> REQUEST_MAP = new ConcurrentHashMap<>();
    private static final ThreadLocal<ScheduledAcquisition> SCHEDULED_ACQUISITION = ThreadLocal.withInitial(ScheduledAcquisition::new);

    private static final Monitor GLOBAL_MONITOR = new Monitor();

    private static final AtomicLong WAIT_COUNTER_NANO = new AtomicLong();

    public static <E, T extends Acquirable<E>> void acquireForEach(@NotNull Collection<? super T> collection,
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
     * Ensure that {@code callback} is safely executed inside the batch thread.
     */
    protected static void acquire(@NotNull Thread currentThread, @Nullable BatchThread elementThread, Runnable callback) {
        if (elementThread == null || elementThread == currentThread) {
            callback.run();
        } else {
            final BatchThread currentBatch = currentThread instanceof BatchThread ? ((BatchThread) currentThread) : null;
            final Monitor currentMonitor = currentBatch != null ? currentBatch.monitor : null;

            boolean enter = false;
            if (currentMonitor != null && currentMonitor.isOccupiedByCurrentThread()) {
                process((BatchThread) currentThread);
                currentMonitor.leave();
                enter = true;
            }

            Monitor monitor = elementThread.monitor;

            if (monitor.isOccupiedByCurrentThread() || GLOBAL_MONITOR.isOccupiedByCurrentThread()) {
                // We already have access to the thread
                callback.run();
            } else if (monitor.tryEnter()) {
                // Acquire the thread
                callback.run();
                monitor.leave();
            } else {
                // Thread is not available, forward request to be executed later

                while (!GLOBAL_MONITOR.tryEnter())
                    processMonitored(currentBatch);

                var requests = getRequests(elementThread);
                Acquirable.Request request = new Acquirable.Request();
                request.localLatch = new CountDownLatch(1);
                request.processLatch = new CountDownLatch(1);
                requests.add(request);

                try {
                    currentBatch.waitingOn = elementThread;
                    request.localLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                currentBatch.waitingOn = null;
                callback.run();
                request.processLatch.countDown();

                GLOBAL_MONITOR.leave();
            }

            if (currentMonitor != null && enter) {
                currentMonitor.enter();
            }
        }
    }

    public static void process(@NotNull BatchThread thread) {
        var requests = getRequests(thread);
        requests.forEach(request -> {
            requests.remove(request);
            request.localLatch.countDown();
            try {
                request.processLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public static void processMonitored(@NotNull BatchThread thread) {
        thread.monitor.enter();
        process(thread);
        thread.monitor.leave();
    }

    private static @NotNull Collection<Acquirable.Request> getRequests(@NotNull BatchThread thread) {
        return REQUEST_MAP.computeIfAbsent(thread, batchThread -> ConcurrentHashMap.newKeySet());
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