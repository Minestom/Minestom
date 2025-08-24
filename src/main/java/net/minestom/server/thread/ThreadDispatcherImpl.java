package net.minestom.server.thread;

import net.minestom.server.Tickable;
import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.MpscUnboundedArrayQueue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.function.IntFunction;

final class ThreadDispatcherImpl<P, E extends Tickable> implements ThreadDispatcher<P, E> {
    private final ThreadProvider<P> provider;
    private final List<TickThread> threads;

    // Partition -> dispatching context
    // Defines how computation is dispatched to the threads
    private final Map<P, Partition> partitions = new WeakHashMap<>();
    // Cache to retrieve the threading context from a tickable element
    private final Map<Tickable, Partition> elements = new WeakHashMap<>();
    // Queue to update partition linked thread
    private final ArrayDeque<P> partitionUpdateQueue = new ArrayDeque<>();

    // Requests consumed at the end of each tick
    private final MessagePassingQueue<Update<P, E>> updates = new MpscUnboundedArrayQueue<>(1024);

    ThreadDispatcherImpl(ThreadProvider<P> provider, int threadCount,
                         IntFunction<? extends TickThread> threadGenerator) {
        this.provider = provider;
        TickThread[] threads = new TickThread[threadCount];
        Arrays.setAll(threads, threadGenerator);
        this.threads = List.of(threads);
    }

    @Unmodifiable
    @ApiStatus.Internal
    @Override
    public List<TickThread> threads() {
        return threads;
    }

    @Override
    public synchronized void updateAndAwait(long time) {
        // Update dispatcher
        this.updates.drain(update -> {
            switch (update) {
                case Update.PartitionLoad<P, E> chunkUpdate -> processLoadedPartition(chunkUpdate.partition());
                case Update.PartitionUnload<P, E> partitionUnload ->
                        processUnloadedPartition(partitionUnload.partition());
                case Update.ElementUpdate<P, E> elementUpdate ->
                        processUpdatedElement(elementUpdate.element(), elementUpdate.partition());
                case Update.ElementRemove<P, E> elementRemove -> processRemovedElement(elementRemove.element());
                case null, default -> throw new IllegalStateException("Unknown update type: " +
                        (update == null ? "null" : update.getClass().getSimpleName()));
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

    @Override
    public synchronized void refreshThreads(long nanoTimeout) {
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
                        previous.entries.remove(partitionEntry);
                        next.entries.add(partitionEntry);
                    }
                    this.partitionUpdateQueue.addLast(partition);
                    if (--counter <= 0 || System.nanoTime() - currentTime >= nanoTimeout) {
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void refreshThreads() {
        refreshThreads(Long.MAX_VALUE);
    }

    @Override
    public synchronized void start() {
        this.threads.forEach(Thread::start);
    }

    @Override
    public boolean isAlive() {
        for (TickThread thread : threads) {
            if (!thread.isAlive()) return false;
        }
        return !threads.isEmpty();
    }

    @Override
    public synchronized void shutdown() {
        this.threads.forEach(TickThread::shutdown);
    }

    private TickThread retrieveThread(P partition) {
        final int threadId = provider.findThread(partition);
        final int index = Math.abs(threadId) % threads.size();
        return threads.get(index);
    }

    @Override
    public void signalUpdate(ThreadDispatcher.Update<P, E> update) {
        this.updates.relaxedOffer(update);
    }

    private void processLoadedPartition(P partition) {
        if (partitions.containsKey(partition)) return;
        final TickThread thread = retrieveThread(partition);
        final Partition partitionEntry = new Partition(thread);
        thread.entries.add(partitionEntry);
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
            thread.entries.remove(partitionEntry);
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
            if (tickable instanceof AcquirableSource<?> acquirableSource) {
                ((AcquirableImpl<?>) acquirableSource.acquirable()).assign(partitionEntry.thread());
            }
        }
    }

    /**
     * A data structure which may contain {@link Tickable}s, and is assigned a single {@link TickThread}.
     */
    public static final class Partition {
        private TickThread thread;
        private final List<Tickable> elements = new ArrayList<>();

        private Partition(TickThread thread) {
            this.thread = thread;
        }

        /**
         * The {@link TickThread} used by this partition.
         * <p>
         * This method is marked internal to reflect {@link TickThread}s own internal status.
         *
         * @return the TickThread used by this partition
         */
        @ApiStatus.Internal
        public TickThread thread() {
            return thread;
        }

        /**
         * The {@link Tickable}s assigned to this partition.
         *
         * @return the tickables assigned to this partition
         */
        public List<Tickable> elements() {
            return elements;
        }
    }
}
