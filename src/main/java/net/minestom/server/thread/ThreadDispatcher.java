package net.minestom.server.thread;

import net.minestom.server.Tickable;
import net.minestom.server.entity.Entity;
import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.MpscUnboundedArrayQueue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * Used to link chunks into multiple groups.
 * Then executed into a thread pool.
 */
public final class ThreadDispatcher<P> {
    private final ThreadProvider<P> provider;
    private final List<TickThread> threads;

    // Partition -> dispatching context
    // Defines how computation is dispatched to the threads
    private final Map<P, Partition> partitions = new WeakHashMap<>();
    // Cache to retrieve the threading context from a tickable element
    private final Map<Tickable, Partition> elements = new WeakHashMap<>();
    // Queue to update chunks linked thread
    private final ArrayDeque<P> partitionUpdateQueue = new ArrayDeque<>();

    // Requests consumed at the end of each tick
    private final MessagePassingQueue<DispatchUpdate<P>> updates = new MpscUnboundedArrayQueue<>(1024);

    private ThreadDispatcher(ThreadProvider<P> provider, int threadCount) {
        this.provider = provider;
        TickThread[] threads = new TickThread[threadCount];
        Arrays.setAll(threads, TickThread::new);
        this.threads = List.of(threads);
        this.threads.forEach(Thread::start);
    }

    public static <P> @NotNull ThreadDispatcher<P> of(@NotNull ThreadProvider<P> provider, int threadCount) {
        return new ThreadDispatcher<>(provider, threadCount);
    }

    public static <P> @NotNull ThreadDispatcher<P> singleThread() {
        return of(ThreadProvider.counter(), 1);
    }

    @Unmodifiable
    public @NotNull List<@NotNull TickThread> threads() {
        return threads;
    }

    /**
     * Prepares the update by creating the {@link TickThread} tasks.
     *
     * @param time the tick time in milliseconds
     */
    public synchronized void updateAndAwait(long time) {
        // Update dispatcher
        this.updates.drain(update -> {
            if (update instanceof DispatchUpdate.PartitionLoad<P> chunkUpdate) {
                processLoadedPartition(chunkUpdate.partition());
            } else if (update instanceof DispatchUpdate.PartitionUnload<P> partitionUnload) {
                processUnloadedPartition(partitionUnload.partition());
            } else if (update instanceof DispatchUpdate.ElementUpdate<P> elementUpdate) {
                processUpdatedElement(elementUpdate.tickable(), elementUpdate.partition());
            } else if (update instanceof DispatchUpdate.ElementRemove elementRemove) {
                processRemovedElement(elementRemove.tickable());
            } else {
                throw new IllegalStateException("Unknown update type: " + update.getClass().getSimpleName());
            }
        });
        // Tick all partitions
        CountDownLatch latch = new CountDownLatch(threads.size());
        for (TickThread thread : threads) thread.startTick(latch, time);
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Called at the end of each tick to clear removed entities,
     * refresh the chunk linked to an entity, and chunk threads based on {@link ThreadProvider#findThread(Object)}.
     *
     * @param nanoTimeout max time in nanoseconds to update partitions
     */
    public void refreshThreads(long nanoTimeout) {
        switch (provider.refreshType()) {
            case NEVER -> {
                // Do nothing
            }
            case ALWAYS -> {
                final long currentTime = System.nanoTime();
                int counter = partitionUpdateQueue.size();
                while (true) {
                    final P partition = partitionUpdateQueue.pollFirst();
                    if (partition == null) break;
                    // Update chunk's thread
                    Partition partitionEntry = partitions.get(partition);
                    assert partitionEntry != null;
                    final TickThread previous = partitionEntry.thread;
                    final TickThread next = retrieveThread(partition);
                    if (next != previous) {
                        partitionEntry.thread = next;
                        previous.entries().remove(partitionEntry);
                        next.entries().add(partitionEntry);
                    }
                    this.partitionUpdateQueue.addLast(partition);
                    if (--counter <= 0 || System.nanoTime() - currentTime >= nanoTimeout) {
                        break;
                    }
                }
            }
        }
    }

    public void refreshThreads() {
        refreshThreads(Long.MAX_VALUE);
    }

    public void createPartition(P partition) {
        signalUpdate(new DispatchUpdate.PartitionLoad<>(partition));
    }

    public void deletePartition(P partition) {
        signalUpdate(new DispatchUpdate.PartitionUnload<>(partition));
    }

    public void updateElement(Tickable tickable, P partition) {
        signalUpdate(new DispatchUpdate.ElementUpdate<>(tickable, partition));
    }

    public void removeElement(Tickable tickable) {
        signalUpdate(new DispatchUpdate.ElementRemove<>(tickable));
    }

    /**
     * Shutdowns all the {@link TickThread tick threads}.
     * <p>
     * Action is irreversible.
     */
    public void shutdown() {
        this.threads.forEach(TickThread::shutdown);
    }

    private TickThread retrieveThread(P partition) {
        final int threadId = provider.findThread(partition);
        final int index = Math.abs(threadId) % threads.size();
        return threads.get(index);
    }

    private void signalUpdate(@NotNull DispatchUpdate<P> update) {
        this.updates.relaxedOffer(update);
    }

    private void processLoadedPartition(P partition) {
        if (partitions.containsKey(partition)) return;
        final TickThread thread = retrieveThread(partition);
        final Partition partitionEntry = new Partition(thread);
        thread.entries().add(partitionEntry);
        this.partitions.put(partition, partitionEntry);
        this.partitionUpdateQueue.add(partition);
        if (partition instanceof Tickable tickable) {
            processUpdatedElement(tickable, partition);
        }
    }

    private void processUnloadedPartition(P partition) {
        final Partition partitionEntry = partitions.remove(partition);
        if (partitionEntry != null) {
            TickThread thread = partitionEntry.thread;
            thread.entries().remove(partitionEntry);
        }
        this.partitionUpdateQueue.remove(partition);
        if (partition instanceof Tickable tickable) {
            processRemovedElement(tickable);
        }
    }

    private void processRemovedElement(Tickable tickable) {
        Partition partition = elements.get(tickable);
        if (partition != null) {
            partition.elements.remove(tickable);
        }
    }

    private void processUpdatedElement(Tickable tickable, P partition) {
        Partition partitionEntry;

        partitionEntry = elements.get(tickable);
        // Remove from previous list
        if (partitionEntry != null) {
            partitionEntry.elements.remove(tickable);
        }
        // Add to new list
        partitionEntry = partitions.get(partition);
        if (partitionEntry != null) {
            this.elements.put(tickable, partitionEntry);
            partitionEntry.elements.add(tickable);
            if (tickable instanceof Entity entity) { // TODO support other types
                ((AcquirableImpl<?>) entity.getAcquirable()).updateThread(partitionEntry.thread());
            }
        }
    }

    public static final class Partition {
        private TickThread thread;
        private final List<Tickable> elements = new ArrayList<>();

        private Partition(TickThread thread) {
            this.thread = thread;
        }

        public @NotNull TickThread thread() {
            return thread;
        }

        public @NotNull List<Tickable> elements() {
            return elements;
        }
    }

    @ApiStatus.Internal
    sealed interface DispatchUpdate<P> permits
            DispatchUpdate.PartitionLoad, DispatchUpdate.PartitionUnload,
            DispatchUpdate.ElementUpdate, DispatchUpdate.ElementRemove {
        record PartitionLoad<P>(@NotNull P partition) implements DispatchUpdate<P> {
        }

        record PartitionUnload<P>(@NotNull P partition) implements DispatchUpdate<P> {
        }

        record ElementUpdate<P>(@NotNull Tickable tickable, P partition) implements DispatchUpdate<P> {
        }

        record ElementRemove<P>(@NotNull Tickable tickable) implements DispatchUpdate<P> {
        }
    }
}
