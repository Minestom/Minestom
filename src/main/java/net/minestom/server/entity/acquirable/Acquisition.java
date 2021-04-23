package net.minestom.server.entity.acquirable;

import net.minestom.server.entity.Entity;
import net.minestom.server.thread.TickThread;
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
     */
    public static void acquireForEach(@NotNull Collection<AcquirableEntity> collection,
                                      @NotNull Consumer<Entity> consumer) {
        final Thread currentThread = Thread.currentThread();
        Map<TickThread, List<Entity>> threadCacheMap = retrieveOptionalThreadMap(collection, currentThread, consumer);

        // Acquire all the threads one by one
        {
            for (Map.Entry<TickThread, List<Entity>> entry : threadCacheMap.entrySet()) {
                final TickThread tickThread = entry.getKey();
                final List<Entity> entities = entry.getValue();

                acquire(currentThread, tickThread, () -> {
                    for (Entity entity : entities) {
                        consumer.accept(entity);
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

        final List<AcquirableEntity> acquirableEntityElements = scheduledAcquisition.acquirableEntityElements;

        if (!acquirableEntityElements.isEmpty()) {
            final Map<Object, List<Consumer<Entity>>> callbacks = scheduledAcquisition.callbacks;

            acquireForEach(acquirableEntityElements, element -> {
                List<Consumer<Entity>> consumers = callbacks.get(element);
                if (consumers == null || consumers.isEmpty())
                    return;
                consumers.forEach(objectConsumer -> objectConsumer.accept(element));
            });

            // Clear collections..
            acquirableEntityElements.clear();
            callbacks.clear();
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

    protected static ReentrantLock acquireEnter(TickThread elementThread) {
        return acquireEnter(Thread.currentThread(), elementThread);
    }

    protected static void acquireLeave(ReentrantLock lock) {
        if (lock != null) {
            lock.unlock();
        }
        GLOBAL_LOCK.unlock();
    }

    protected synchronized static void scheduledAcquireRequest(@NotNull AcquirableEntity acquirableEntity, Consumer<Entity> consumer) {
        ScheduledAcquisition scheduledAcquisition = SCHEDULED_ACQUISITION.get();
        scheduledAcquisition.acquirableEntityElements.add(acquirableEntity);
        scheduledAcquisition.callbacks
                .computeIfAbsent(acquirableEntity.unwrap(), objectAcquirable -> new ArrayList<>())
                .add(consumer);
    }

    /**
     * Creates
     *
     * @param collection    the acquirable collection
     * @param currentThread the current thread
     * @param consumer      the consumer to execute when an element is already in the current thread
     * @return a new Thread to acquirable elements map
     */
    protected static Map<TickThread, List<Entity>> retrieveOptionalThreadMap(@NotNull Collection<AcquirableEntity> collection,
                                                                             @NotNull Thread currentThread,
                                                                             @NotNull Consumer<? super Entity> consumer) {
        // Separate a collection of acquirable elements into a map of thread->elements
        // Useful to reduce the number of acquisition

        Map<TickThread, List<Entity>> threadCacheMap = new HashMap<>();
        for (AcquirableEntity element : collection) {
            final Entity value = element.unwrap();

            final TickThread elementThread = element.getHandler().getTickThread();
            if (currentThread == elementThread) {
                // The element is managed in the current thread, consumer can be immediately called
                consumer.accept(value);
            } else {
                // The element is manager in a different thread, cache it
                List<Entity> threadCacheList = threadCacheMap.computeIfAbsent(elementThread, tickThread -> new ArrayList<>());
                threadCacheList.add(value);
            }
        }

        return threadCacheMap;
    }

    protected static Map<TickThread, List<Entity>> retrieveThreadMap(@NotNull Collection<AcquirableEntity> collection) {
        // Separate a collection of acquirable elements into a map of thread->elements
        // Useful to reduce the number of acquisition
        Map<TickThread, List<Entity>> threadCacheMap = new HashMap<>();
        for (AcquirableEntity acquirableEntity : collection) {
            final Entity entity = acquirableEntity.unwrap();
            final TickThread elementThread = acquirableEntity.getHandler().getTickThread();

            List<Entity> threadCacheList = threadCacheMap.computeIfAbsent(elementThread, tickThread -> new ArrayList<>());
            threadCacheList.add(entity);
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
        private final List<AcquirableEntity> acquirableEntityElements = new ArrayList<>();
        private final Map<Object, List<Consumer<Entity>>> callbacks = new HashMap<>();
    }
}
