package net.minestom.server.lock;

import net.minestom.server.MinecraftServer;
import net.minestom.server.thread.BatchQueue;
import net.minestom.server.thread.BatchThread;
import net.minestom.server.thread.batch.BatchInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class Acquisition {

    private static final ScheduledExecutorService ACQUISITION_CONTENTION_SERVICE = Executors.newSingleThreadScheduledExecutor();
    private static final ThreadLocal<List<Thread>> ACQUIRED_THREADS = ThreadLocal.withInitial(ArrayList::new);

    private static final ThreadLocal<ScheduledAcquisition> SCHEDULED_ACQUISITION = ThreadLocal.withInitial(ScheduledAcquisition::new);

    private static final AtomicLong WAIT_COUNTER_NANO = new AtomicLong();

    static {
        // The goal of the contention service it is manage the situation where two threads are waiting for each other
        ACQUISITION_CONTENTION_SERVICE.scheduleAtFixedRate(() -> {

            final List<BatchThread> threads = MinecraftServer.getUpdateManager().getThreadProvider().getThreads();

            for (BatchThread batchThread : threads) {
                final BatchThread waitingThread = (BatchThread) batchThread.getQueue().getWaitingThread();
                if (waitingThread != null) {
                    if (waitingThread.getState() == Thread.State.WAITING &&
                            batchThread.getState() == Thread.State.WAITING) {
                        processQueue(waitingThread.getQueue());
                    }
                }
            }

        }, 3, 3, TimeUnit.MILLISECONDS);
    }

    public static <E, T extends Acquirable<E>> void acquireCollection(@NotNull Collection<T> collection,
                                                                      @NotNull Supplier<Collection<E>> collectionSupplier,
                                                                      @NotNull Consumer<Collection<E>> consumer) {
        final Thread currentThread = Thread.currentThread();
        Collection<E> result = collectionSupplier.get();

        Map<BatchThread, List<E>> threadCacheMap = retrieveThreadMap(collection, currentThread, result::add);

        // Acquire all the threads
        {
            List<Phaser> phasers = new ArrayList<>();

            for (Map.Entry<BatchThread, List<E>> entry : threadCacheMap.entrySet()) {
                final BatchThread batchThread = entry.getKey();
                final List<E> elements = entry.getValue();

                AcquisitionData data = new AcquisitionData();

                acquire(currentThread, batchThread, data);

                // Retrieve all elements
                result.addAll(elements);

                final Phaser phaser = data.getPhaser();
                if (phaser != null) {
                    phasers.add(phaser);
                }
            }

            // Give result and deregister phasers
            consumer.accept(result);
            for (Phaser phaser : phasers) {
                phaser.arriveAndDeregister();
            }

        }
    }

    public static <E, T extends Acquirable<E>> void acquireForEach(@NotNull Collection<T> collection,
                                                                   @NotNull Consumer<E> consumer) {
        final Thread currentThread = Thread.currentThread();
        Map<BatchThread, List<E>> threadCacheMap = retrieveThreadMap(collection, currentThread, consumer);

        // Acquire all the threads one by one
        {
            for (Map.Entry<BatchThread, List<E>> entry : threadCacheMap.entrySet()) {
                final BatchThread batchThread = entry.getKey();
                final List<E> elements = entry.getValue();

                AcquisitionData data = new AcquisitionData();

                acquire(currentThread, batchThread, data);

                // Execute the consumer for all waiting elements
                for (E element : elements) {
                    synchronized (element) {
                        consumer.accept(element);
                    }
                }

                final Phaser phaser = data.getPhaser();
                if (phaser != null) {
                    phaser.arriveAndDeregister();
                }
            }
        }
    }

    /**
     * Notifies all the locks and wait for them to return using a {@link Phaser}.
     * <p>
     * Currently called during instance/chunk/entity ticks
     * and in {@link BatchThread.BatchRunnable#run()} after every thread-tick.
     *
     * @param queue the queue to empty containing the locks to notify
     * @see #acquire(Thread, BatchThread, AcquisitionData)
     */
    public static void processQueue(@NotNull BatchQueue queue) {
        Queue<AcquisitionData> acquisitionQueue = queue.getQueue();

        if (acquisitionQueue.isEmpty())
            return;

        Phaser phaser = new Phaser(1);
        synchronized (queue) {
            AcquisitionData lock;
            while ((lock = acquisitionQueue.poll()) != null) {
                lock.phaser = phaser;
                phaser.register();
            }

            queue.setWaitingThread(null);
            queue.notifyAll();
        }

        phaser.arriveAndAwaitAdvance();
    }

    public static void processThreadTick(@NotNull BatchQueue queue) {
        processQueue(queue);

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
     * Checks if the {@link Acquirable} update tick is in the same thread as {@link Thread#currentThread()}.
     * If yes return immediately, otherwise a lock will be created and added to {@link BatchQueue#getQueue()}
     * to be executed later during {@link #processQueue(BatchQueue)}.
     *
     * @param data the object containing data about the acquisition
     * @return true if the acquisition didn't require any synchronization
     * @see #processQueue(BatchQueue)
     */
    protected static boolean acquire(@NotNull Thread currentThread, @Nullable BatchThread elementThread, @NotNull AcquisitionData data) {
        if (elementThread == null) {
            // Element didn't get assigned a thread yet (meaning that the element is not part of any thread)
            // Returns false in order to force synchronization (useful if this element is acquired multiple time)
            return false;
        }

        if (currentThread == elementThread) {
            // Element can be acquired without any wait/block because threads are the same
            return true;
        }

        if (!elementThread.getMainRunnable().isInTick()) {
            // Element tick has ended and can therefore be directly accessed (with synchronization)
            return false;
        }

        final List<Thread> acquiredThread = ACQUIRED_THREADS.get();
        if (acquiredThread.contains(elementThread)) {
            // This thread is already acquiring the element thread
            return true;
        }

        // Element needs to be synchronized, forward a request
        {
            // Prevent most of contentions, the rest in handled in the acquisition scheduled service
            if (currentThread instanceof BatchThread) {
                BatchThread batchThread = (BatchThread) currentThread;
                Acquisition.processQueue(batchThread.getQueue());
            }

            try {
                final boolean monitoring = MinecraftServer.hasWaitMonitoring();
                long time = 0;
                if (monitoring) {
                    time = System.nanoTime();
                }

                final BatchQueue periodQueue = elementThread.getQueue();
                synchronized (periodQueue) {
                    acquiredThread.add(elementThread);
                    data.acquiredThreads = acquiredThread; // Shared to remove the element when the acquisition is done

                    periodQueue.setWaitingThread(elementThread);
                    periodQueue.getQueue().add(data);
                    periodQueue.wait();
                }

                if (monitoring) {
                    time = System.nanoTime() - time;
                    WAIT_COUNTER_NANO.addAndGet(time);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return false;
        }
    }

    protected synchronized static <T> void scheduledAcquireRequest(@NotNull Acquirable<T> acquirable, Consumer<T> consumer) {
        ScheduledAcquisition scheduledAcquisition = SCHEDULED_ACQUISITION.get();
        scheduledAcquisition.acquirableElements.add((Acquirable<Object>) acquirable);
        scheduledAcquisition.callbacks
                .computeIfAbsent(acquirable.unwrap(), objectAcquirable -> new ArrayList<>())
                .add((Consumer<Object>) consumer);
    }

    private static <E, T extends Acquirable<E>> Map<BatchThread, List<E>> retrieveThreadMap(@NotNull Collection<T> collection,
                                                                                            @NotNull Thread currentThread,
                                                                                            @NotNull Consumer<E> consumer) {
        Map<BatchThread, List<E>> threadCacheMap = new HashMap<>();

        for (T element : collection) {
            final E value = element.unwrap();

            final BatchInfo batchInfo = element.getHandler().getBatchInfo();
            final BatchThread elementThread = batchInfo != null ? batchInfo.getBatchThread() : null;
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

    public static final class AcquisitionData {

        private volatile Phaser phaser;
        private volatile List<Thread> acquiredThreads;

        @Nullable
        public Phaser getPhaser() {
            return phaser;
        }

        @Nullable
        public List<Thread> getAcquiredThreads() {
            return acquiredThreads;
        }
    }

    private static class ScheduledAcquisition {
        private final List<Acquirable<Object>> acquirableElements = new ArrayList<>();
        private final Map<Object, List<Consumer<Object>>> callbacks = new HashMap<>();
    }
}