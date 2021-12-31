package net.minestom.server.thread;

import net.minestom.server.MinecraftServer;
import net.minestom.server.Tickable;
import net.minestom.server.acquirable.Acquirable;
import net.minestom.server.utils.MathUtils;
import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.MpscUnboundedArrayQueue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.Phaser;

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

    private final Phaser phaser = new Phaser(1);

    private ThreadDispatcher(ThreadProvider<P> provider, int threadCount) {
        this.provider = provider;
        TickThread[] threads = new TickThread[threadCount];
        Arrays.setAll(threads, i -> new TickThread(phaser, i));
        this.threads = List.of(threads);
        this.threads.forEach(Thread::start);
    }

    public static <T> @NotNull ThreadDispatcher<T> of(@NotNull ThreadProvider<T> provider, int threadCount) {
        return new ThreadDispatcher<>(provider, threadCount);
    }

    public static <T> @NotNull ThreadDispatcher<T> singleThread() {
        return of(ThreadProvider.counter(), 1);
    }

    public @NotNull List<@NotNull TickThread> threads() {
        return threads;
    }

    /**
     * Represents the maximum percentage of tick time that can be spent refreshing chunks thread.
     * <p>
     * Percentage based on {@link MinecraftServer#TICK_MS}.
     *
     * @return the refresh percentage
     */
    public float getRefreshPercentage() {
        return 0.3f;
    }

    /**
     * Minimum time used to refresh chunks and entities thread.
     *
     * @return the minimum refresh time in milliseconds
     */
    public int getMinimumRefreshTime() {
        return 3;
    }

    /**
     * Maximum time used to refresh chunks and entities thread.
     *
     * @return the maximum refresh time in milliseconds
     */
    public int getMaximumRefreshTime() {
        return (int) (MinecraftServer.TICK_MS * 0.3);
    }

    /**
     * Prepares the update by creating the {@link TickThread} tasks.
     *
     * @param time the tick time in milliseconds
     */
    public void updateAndAwait(long time) {
        // Update dispatcher
        this.updates.drain(update -> {
            if (update instanceof DispatchUpdate.PartitionLoad<P> chunkUpdate) {
                processLoadedChunk(chunkUpdate.partition());
            } else if (update instanceof DispatchUpdate.PartitionUnload<P> partitionUnload) {
                processUnloadedChunk(partitionUnload.partition());
            } else if (update instanceof DispatchUpdate.ElementUpdate<P> elementUpdate) {
                processUpdatedElement(elementUpdate.tickable(), elementUpdate.partition());
            } else if (update instanceof DispatchUpdate.ElementRemove elementRemove) {
                processRemovedEntity(elementRemove.tickable());
            } else {
                throw new IllegalStateException("Unknown update type: " + update.getClass().getSimpleName());
            }
        });
        // Tick all partitions
        for (TickThread thread : threads) thread.startTick(time);
        this.phaser.arriveAndAwaitAdvance();
    }

    /**
     * Called at the end of each tick to clear removed entities,
     * refresh the chunk linked to an entity, and chunk threads based on {@link ThreadProvider#findThread(Object)}.
     *
     * @param tickTime the duration of the tick in ms,
     *                 used to ensure that the refresh does not take more time than the tick itself
     */
    public void refreshThreads(long tickTime) {
        final ThreadProvider.RefreshType refreshType = provider.getChunkRefreshType();
        if (refreshType == ThreadProvider.RefreshType.NEVER)
            return;

        final int timeOffset = MathUtils.clamp((int) ((double) tickTime * getRefreshPercentage()),
                getMinimumRefreshTime(), getMaximumRefreshTime());
        final long endTime = System.currentTimeMillis() + timeOffset;
        final int size = partitionUpdateQueue.size();
        int counter = 0;
        while (true) {
            final P partition = partitionUpdateQueue.pollFirst();
            if (partition == null) break;
            // Update chunk's thread
            Partition partitionEntry = partitions.get(partition);
            if (partitionEntry != null) partitionEntry.thread = retrieveThread(partition);
            this.partitionUpdateQueue.addLast(partition);
            if (++counter > size || System.currentTimeMillis() >= endTime)
                break;
        }
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

    public void signalUpdate(@NotNull DispatchUpdate<P> update) {
        this.updates.relaxedOffer(update);
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
        final int threadId = Math.abs(provider.findThread(partition)) % threads.size();
        return threads.get(threadId);
    }

    private void processLoadedChunk(P partition) {
        if(partitions.containsKey(partition)) return;
        final TickThread thread = retrieveThread(partition);
        final Partition partitionEntry = new Partition(thread);
        thread.entries().add(partitionEntry);
        this.partitions.put(partition, partitionEntry);
        this.partitionUpdateQueue.add(partition);
        if (partition instanceof Tickable tickable) {
            processUpdatedElement(tickable, partition);
        }
    }

    private void processUnloadedChunk(P partition) {
        final Partition partitionEntry = partitions.remove(partition);
        if (partitionEntry != null) {
            TickThread thread = partitionEntry.thread;
            thread.entries().remove(partitionEntry);
        }
        this.partitionUpdateQueue.remove(partition);
    }

    private void processRemovedEntity(Tickable tickable) {
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
            if (tickable instanceof Acquirable<?> acquirable) {
                acquirable.getHandler().refreshChunkEntry(partitionEntry);
            }
        }
    }

    public static final class Partition {
        private volatile TickThread thread;
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
