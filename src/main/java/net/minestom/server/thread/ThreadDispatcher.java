package net.minestom.server.thread;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Chunk;
import net.minestom.server.utils.MathUtils;
import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.MpscUnboundedArrayQueue;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.Phaser;

/**
 * Used to link chunks into multiple groups.
 * Then executed into a thread pool.
 */
public final class ThreadDispatcher {
    private final ThreadProvider provider;
    private final List<TickThread> threads;

    // Chunk -> ChunkEntry mapping
    private final Map<Chunk, ChunkEntry> chunkEntryMap = new HashMap<>();
    // Queue to update chunks linked thread
    private final ArrayDeque<Chunk> chunkUpdateQueue = new ArrayDeque<>();

    // Requests consumed at the end of each tick
    private final MessagePassingQueue<DispatchUpdate> updates = new MpscUnboundedArrayQueue<>(1024);

    private final Phaser phaser = new Phaser(1);

    private ThreadDispatcher(ThreadProvider provider, int threadCount) {
        this.provider = provider;
        TickThread[] threads = new TickThread[threadCount];
        Arrays.setAll(threads, i -> new TickThread(phaser, i));
        this.threads = List.of(threads);
        this.threads.forEach(Thread::start);
    }

    public static @NotNull ThreadDispatcher of(@NotNull ThreadProvider provider, int threadCount) {
        return new ThreadDispatcher(provider, threadCount);
    }

    public static @NotNull ThreadDispatcher singleThread() {
        return of(ThreadProvider.SINGLE, 1);
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
        for (TickThread thread : threads) thread.startTick(time);
        this.phaser.arriveAndAwaitAdvance();
        // Update dispatcher
        this.updates.drain(update -> {
            if (update instanceof DispatchUpdate.ChunkLoad chunkUpdate) {
                processLoadedChunk(chunkUpdate.chunk());
            } else if (update instanceof DispatchUpdate.ChunkUnload chunkUnload) {
                processUnloadedChunk(chunkUnload.chunk());
            } else if (update instanceof DispatchUpdate.EntityUpdate entityUpdate) {
                processUpdatedEntity(entityUpdate.entity());
            } else if (update instanceof DispatchUpdate.EntityRemove entityRemove) {
                processRemovedEntity(entityRemove.entity());
            } else {
                throw new IllegalStateException("Unknown update type: " + update.getClass().getSimpleName());
            }
        });
    }

    /**
     * Called at the end of each tick to clear removed entities,
     * refresh the chunk linked to an entity, and chunk threads based on {@link ThreadProvider#findThread(Chunk)}.
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
        final int size = chunkUpdateQueue.size();
        int counter = 0;
        while (true) {
            final Chunk chunk = chunkUpdateQueue.pollFirst();
            if (chunk == null) break;
            // Update chunk's thread
            ChunkEntry chunkEntry = chunkEntryMap.get(chunk);
            if (chunkEntry != null) chunkEntry.thread = retrieveThread(chunk);
            this.chunkUpdateQueue.addLast(chunk);
            if (++counter > size || System.currentTimeMillis() >= endTime)
                break;
        }
    }

    public void signalUpdate(@NotNull DispatchUpdate update) {
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

    private TickThread retrieveThread(Chunk chunk) {
        final int threadId = Math.abs(provider.findThread(chunk)) % threads.size();
        return threads.get(threadId);
    }

    private void processLoadedChunk(Chunk chunk) {
        final TickThread thread = retrieveThread(chunk);
        final ChunkEntry chunkEntry = new ChunkEntry(thread, chunk);
        thread.entries().add(chunkEntry);
        this.chunkEntryMap.put(chunk, chunkEntry);
        this.chunkUpdateQueue.add(chunk);
    }

    private void processUnloadedChunk(Chunk chunk) {
        final ChunkEntry chunkEntry = chunkEntryMap.remove(chunk);
        if (chunkEntry != null) {
            TickThread thread = chunkEntry.thread;
            thread.entries().remove(chunkEntry);
        }
        this.chunkUpdateQueue.remove(chunk);
    }

    private void processRemovedEntity(Entity entity) {
        var acquirableEntity = entity.getAcquirable();
        ChunkEntry chunkEntry = acquirableEntity.getHandler().getChunkEntry();
        if (chunkEntry != null) {
            chunkEntry.entities.remove(entity);
        }
    }

    private void processUpdatedEntity(Entity entity) {
        ChunkEntry chunkEntry;

        var acquirableEntity = entity.getAcquirable();
        chunkEntry = acquirableEntity.getHandler().getChunkEntry();
        // Remove from previous list
        if (chunkEntry != null) {
            chunkEntry.entities.remove(entity);
        }
        // Add to new list
        chunkEntry = chunkEntryMap.get(entity.getChunk());
        if (chunkEntry != null) {
            chunkEntry.entities.add(entity);
            acquirableEntity.getHandler().refreshChunkEntry(chunkEntry);
        }
    }

    public static final class ChunkEntry {
        private volatile TickThread thread;
        private final Chunk chunk;
        private final List<Entity> entities = new ArrayList<>();

        private ChunkEntry(TickThread thread, Chunk chunk) {
            this.thread = thread;
            this.chunk = chunk;
        }

        public @NotNull TickThread thread() {
            return thread;
        }

        public @NotNull Chunk chunk() {
            return chunk;
        }

        public @NotNull List<Entity> entities() {
            return entities;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ChunkEntry that = (ChunkEntry) o;
            return chunk.equals(that.chunk);
        }

        @Override
        public int hashCode() {
            return Objects.hash(chunk);
        }
    }
}
