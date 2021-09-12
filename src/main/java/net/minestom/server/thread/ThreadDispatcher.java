package net.minestom.server.thread;

import net.minestom.server.MinecraftServer;
import net.minestom.server.acquirable.Acquirable;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Phaser;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * Used to link chunks into multiple groups.
 * Then executed into a thread pool.
 */
public final class ThreadDispatcher {
    private final ThreadProvider provider;
    private final List<TickThread> threads;

    private final Map<TickThread, Set<ChunkEntry>> threadChunkMap = new HashMap<>();
    private final Map<Chunk, ChunkEntry> chunkEntryMap = new HashMap<>();
    private final ArrayDeque<Chunk> chunkUpdateQueue = new ArrayDeque<>();

    private final Queue<Chunk> chunkLoadRequests = new ConcurrentLinkedQueue<>();
    private final Queue<Chunk> chunkUnloadRequests = new ConcurrentLinkedQueue<>();
    private final Queue<Entity> entityUpdateRequests = new ConcurrentLinkedQueue<>();
    private final Queue<Entity> entityRemovalRequests = new ConcurrentLinkedQueue<>();

    private final Phaser phaser = new Phaser(1);

    private ThreadDispatcher(ThreadProvider provider, int threadCount) {
        this.provider = provider;
        this.threads = new ArrayList<>(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final TickThread.BatchRunnable batchRunnable = new TickThread.BatchRunnable();
            final TickThread tickThread = new TickThread(batchRunnable, i);
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
        for (var entry : threadChunkMap.entrySet()) {
            final TickThread thread = entry.getKey();
            final Set<ChunkEntry> chunkEntries = entry.getValue();
            if (chunkEntries == null || chunkEntries.isEmpty()) {
                // Nothing to tick
                continue;
            }
            // Execute tick
            this.phaser.register();
            thread.runnable.startTick(phaser, () -> {
                Acquirable.refreshEntries(chunkEntries);

                final ReentrantLock lock = thread.getLock();
                lock.lock();
                for (ChunkEntry chunkEntry : chunkEntries) {
                    final Chunk chunk = chunkEntry.chunk;
                    if (!ChunkUtils.isLoaded(chunk)) return;
                    try {
                        chunk.tick(time);
                    } catch (Throwable e) {
                        MinecraftServer.getExceptionManager().handleException(e);
                    }
                    final List<Entity> entities = chunkEntry.entities;
                    if (!entities.isEmpty()) {
                        for (Entity entity : entities) {
                            if (lock.hasQueuedThreads()) {
                                lock.unlock();
                                // #acquire() callbacks should be called here
                                lock.lock();
                            }
                            try {
                                entity.tick(time);
                            } catch (Throwable e) {
                                MinecraftServer.getExceptionManager().handleException(e);
                            }
                        }
                    }
                }
                lock.unlock();
                // #acquire() callbacks
            });
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
        processRemovedEntities();
        processUpdatedEntities();
        if (provider.getChunkRefreshType() == ThreadProvider.RefreshType.NEVER)
            return;

        final int timeOffset = MathUtils.clamp((int) ((double) tickTime * getRefreshPercentage()),
                getMinimumRefreshTime(), getMaximumRefreshTime());
        final long endTime = System.currentTimeMillis() + timeOffset;
        final int size = chunkUpdateQueue.size();
        int counter = 0;
        while (true) {
            final Chunk chunk = chunkUpdateQueue.pollFirst();
            if (!ChunkUtils.isLoaded(chunk)) {
                removeChunk(chunk);
                continue;
            }
            // Update chunk threads
            switchChunk(chunk);
            // Add back to the deque
            chunkUpdateQueue.addLast(chunk);

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

    private void switchChunk(@NotNull Chunk chunk) {
        ChunkEntry chunkEntry = chunkEntryMap.get(chunk);
        if (chunkEntry == null) return;
        Set<ChunkEntry> chunks = threadChunkMap.get(chunkEntry.thread);
        if (chunks == null || chunks.isEmpty()) return;
        chunks.remove(chunkEntry);
        setChunkThread(chunk, tickThread -> {
            chunkEntry.thread = tickThread;
            return chunkEntry;
        });
    }

    private @NotNull ChunkEntry setChunkThread(@NotNull Chunk chunk,
                                               @NotNull Function<TickThread, ChunkEntry> chunkEntrySupplier) {
        final int threadId = Math.abs(provider.findThread(chunk)) % threads.size();
        TickThread thread = threads.get(threadId);
        Set<ChunkEntry> chunks = threadChunkMap.computeIfAbsent(thread, tickThread -> new HashSet<>());

        ChunkEntry chunkEntry = chunkEntrySupplier.apply(thread);
        chunks.add(chunkEntry);
        return chunkEntry;
    }

    private void removeChunk(Chunk chunk) {
        ChunkEntry chunkEntry = chunkEntryMap.get(chunk);
        if (chunkEntry != null) {
            TickThread thread = chunkEntry.thread;
            Set<ChunkEntry> chunks = threadChunkMap.get(thread);
            if (chunks != null) {
                chunks.remove(chunkEntry);
            }
            chunkEntryMap.remove(chunk);
        }
        this.chunkUpdateQueue.remove(chunk);
    }

    private void processLoadedChunks() {
        Chunk chunk;
        while ((chunk = chunkLoadRequests.poll()) != null) {
            Chunk finalChunk = chunk;
            ChunkEntry chunkEntry = setChunkThread(chunk, (thread) -> new ChunkEntry(thread, finalChunk));
            this.chunkEntryMap.put(chunk, chunkEntry);
            this.chunkUpdateQueue.add(chunk);
        }
    }

    private void processUnloadedChunks() {
        Chunk chunk;
        while ((chunk = chunkUnloadRequests.poll()) != null) {
            removeChunk(chunk);
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

        public @NotNull TickThread getThread() {
            return thread;
        }

        public @NotNull Chunk getChunk() {
            return chunk;
        }

        public @NotNull List<Entity> getEntities() {
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
