package net.minestom.server.thread;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
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
    private final Queue<Chunk> chunkLoadRequests = new ConcurrentLinkedQueue<>();
    private final Queue<Chunk> chunkUnloadRequests = new ConcurrentLinkedQueue<>();
    private final Queue<Entity> entityUpdateRequests = new ConcurrentLinkedQueue<>();
    private final Queue<Entity> entityRemovalRequests = new ConcurrentLinkedQueue<>();

    private final Phaser phaser = new Phaser(1);

    private ThreadDispatcher(ThreadProvider provider, int threadCount) {
        this.provider = provider;
        this.threads = new ArrayList<>(threadCount);
        for (int i = 0; i < threadCount; i++) {
            final TickThread tickThread = new TickThread(phaser, i);
            this.threads.add(tickThread);
            tickThread.start();
        }
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
        for (TickThread thread : threads) {
            thread.startTick(time);
        }
        this.phaser.arriveAndAwaitAdvance();
    }

    /**
     * Called at the end of each tick to clear removed entities,
     * refresh the chunk linked to an entity, and chunk threads based on {@link ThreadProvider#findThread(Chunk)}.
     *
     * @param tickTime the duration of the tick in ms,
     *                 used to ensure that the refresh does not take more time than the tick itself
     */
    public void refreshThreads(long tickTime) {
        processLoadedChunks();
        processUnloadedChunks();
        processUpdatedEntities();
        processRemovedEntities();
        if (provider.getChunkRefreshType() == ThreadProvider.RefreshType.NEVER)
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

    /**
     * Shutdowns all the {@link TickThread tick threads}.
     * <p>
     * Action is irreversible.
     */
    public void shutdown() {
        this.threads.forEach(TickThread::shutdown);
    }

    public void onInstanceCreate(@NotNull Instance instance) {
        instance.getChunks().forEach(this::onChunkLoad);
    }

    public void onInstanceDelete(@NotNull Instance instance) {
        instance.getChunks().forEach(this::onChunkUnload);
    }

    public void onChunkLoad(Chunk chunk) {
        this.chunkLoadRequests.add(chunk);
    }

    public void onChunkUnload(Chunk chunk) {
        this.chunkUnloadRequests.add(chunk);
    }

    public void updateEntity(@NotNull Entity entity) {
        this.entityUpdateRequests.add(entity);
    }

    public void removeEntity(@NotNull Entity entity) {
        this.entityRemovalRequests.add(entity);
    }

    private TickThread retrieveThread(Chunk chunk) {
        final int threadId = Math.abs(provider.findThread(chunk)) % threads.size();
        return threads.get(threadId);
    }

    private void processLoadedChunks() {
        Chunk chunk;
        while ((chunk = chunkLoadRequests.poll()) != null) {
            final TickThread thread = retrieveThread(chunk);
            final ChunkEntry chunkEntry = new ChunkEntry(thread, chunk);
            thread.entries().add(chunkEntry);
            this.chunkEntryMap.put(chunk, chunkEntry);
            this.chunkUpdateQueue.add(chunk);
        }
    }

    private void processUnloadedChunks() {
        Chunk chunk;
        while ((chunk = chunkUnloadRequests.poll()) != null) {
            final ChunkEntry chunkEntry = chunkEntryMap.remove(chunk);
            if (chunkEntry != null) {
                TickThread thread = chunkEntry.thread;
                thread.entries().remove(chunkEntry);
            }
            this.chunkUpdateQueue.remove(chunk);
        }
    }

    private void processRemovedEntities() {
        Entity entity;
        while ((entity = entityRemovalRequests.poll()) != null) {
            var acquirableEntity = entity.getAcquirable();
            ChunkEntry chunkEntry = acquirableEntity.getHandler().getChunkEntry();
            if (chunkEntry != null) {
                chunkEntry.entities.remove(entity);
            }
        }
    }

    private void processUpdatedEntities() {
        Entity entity;
        while ((entity = entityUpdateRequests.poll()) != null) {
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
