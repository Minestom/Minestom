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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Phaser;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * Used to link chunks into multiple groups.
 * Then executed into a thread pool.
 */
public abstract class ThreadProvider {

    private final List<TickThread> threads;

    private final Map<TickThread, Set<ChunkEntry>> threadChunkMap = new HashMap<>();
    private final Map<Chunk, ChunkEntry> chunkEntryMap = new HashMap<>();
    // Iterated over to refresh the thread used to update entities & chunks
    private final ArrayDeque<Chunk> chunks = new ArrayDeque<>();
    private final Set<Entity> updatableEntities = ConcurrentHashMap.newKeySet();
    private final Set<Entity> removedEntities = ConcurrentHashMap.newKeySet();

    private final Phaser phaser = new Phaser(1);

    public ThreadProvider(int threadCount) {
        this.threads = new ArrayList<>(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final TickThread.BatchRunnable batchRunnable = new TickThread.BatchRunnable();
            final TickThread tickThread = new TickThread(batchRunnable, i);
            this.threads.add(tickThread);

            tickThread.start();
        }
    }

    public ThreadProvider() {
        this(Runtime.getRuntime().availableProcessors());
    }

    public synchronized void onInstanceCreate(@NotNull Instance instance) {
        instance.getChunks().forEach(this::addChunk);
    }

    public synchronized void onInstanceDelete(@NotNull Instance instance) {
        instance.getChunks().forEach(this::removeChunk);
    }

    public synchronized void onChunkLoad(Chunk chunk) {
        addChunk(chunk);
    }

    public synchronized void onChunkUnload(Chunk chunk) {
        removeChunk(chunk);
    }

    /**
     * Performs a server tick for all chunks based on their linked thread.
     *
     * @param chunk the chunk
     */
    public abstract int findThread(@NotNull Chunk chunk);

    /**
     * Defines how often chunks thread should be updated.
     *
     * @return the refresh type
     */
    public @NotNull RefreshType getChunkRefreshType() {
        return RefreshType.CONSTANT;
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
            final var chunkEntries = entry.getValue();
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
                for (var chunkEntry : chunkEntries) {
                    final Chunk chunk = chunkEntry.chunk;
                    if (!ChunkUtils.isLoaded(chunk))
                        return;
                    try {
                        chunk.tick(time);
                    } catch (Exception e) {
                        MinecraftServer.getExceptionManager().handleException(e);
                    }
                    final var entities = chunkEntry.entities;
                    if (!entities.isEmpty()) {
                        for (Entity entity : entities) {
                            if (lock.hasQueuedThreads()) {
                                lock.unlock();
                                // #acquire() callbacks should be called here
                                lock.lock();
                            }
                            try {
                                entity.tick(time);
                            } catch (Exception e) {
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
     * refresh the chunk linked to an entity, and chunk threads based on {@link #findThread(Chunk)}.
     *
     * @param tickTime the duration of the tick in ms,
     *                 used to ensure that the refresh does not take more time than the tick itself
     */
    public synchronized void refreshThreads(long tickTime) {
        // Clear removed entities
        processRemovedEntities();
        // Update entities chunks
        processUpdatedEntities();
        if (getChunkRefreshType() == RefreshType.NEVER)
            return;

        final int timeOffset = MathUtils.clamp((int) ((double) tickTime * getRefreshPercentage()),
                getMinimumRefreshTime(), getMaximumRefreshTime());
        final long endTime = System.currentTimeMillis() + timeOffset;
        final int size = chunks.size();
        int counter = 0;
        while (true) {
            final Chunk chunk = chunks.pollFirst();
            if (!ChunkUtils.isLoaded(chunk)) {
                removeChunk(chunk);
                continue;
            }
            // Update chunk threads
            switchChunk(chunk);
            // Add back to the deque
            chunks.addLast(chunk);

            if (++counter > size)
                break;
            if (System.currentTimeMillis() >= endTime)
                break;
        }
    }

    /**
     * Add an entity into the waiting list to get assigned in a thread.
     * <p>
     * Called when entering a new chunk.
     *
     * @param entity the entity to add
     */
    public void updateEntity(@NotNull Entity entity) {
        this.updatableEntities.add(entity);
    }

    /**
     * Add an entity into the waiting list to get removed from its thread.
     * <p>
     * Called in {@link Entity#remove()}.
     *
     * @param entity the entity to remove
     */
    public void removeEntity(@NotNull Entity entity) {
        this.removedEntities.add(entity);
    }

    /**
     * Shutdowns all the {@link TickThread tick threads}.
     * <p>
     * Action is irreversible.
     */
    public void shutdown() {
        this.threads.forEach(TickThread::shutdown);
    }

    private void addChunk(@NotNull Chunk chunk) {
        ChunkEntry chunkEntry = setChunkThread(chunk, (thread) -> new ChunkEntry(thread, chunk));
        this.chunkEntryMap.put(chunk, chunkEntry);
        this.chunks.add(chunk);
    }

    private void switchChunk(@NotNull Chunk chunk) {
        ChunkEntry chunkEntry = chunkEntryMap.get(chunk);
        if (chunkEntry == null)
            return;
        var chunks = threadChunkMap.get(chunkEntry.thread);
        if (chunks == null || chunks.isEmpty())
            return;
        chunks.remove(chunkEntry);

        setChunkThread(chunk, tickThread -> {
            chunkEntry.thread = tickThread;
            return chunkEntry;
        });
    }

    private @NotNull ChunkEntry setChunkThread(@NotNull Chunk chunk,
                                               @NotNull Function<TickThread, ChunkEntry> chunkEntrySupplier) {
        final int threadId = Math.abs(findThread(chunk)) % threads.size();
        TickThread thread = threads.get(threadId);
        var chunks = threadChunkMap.computeIfAbsent(thread, tickThread -> ConcurrentHashMap.newKeySet());

        ChunkEntry chunkEntry = chunkEntrySupplier.apply(thread);
        chunks.add(chunkEntry);
        return chunkEntry;
    }

    private void removeChunk(Chunk chunk) {
        ChunkEntry chunkEntry = chunkEntryMap.get(chunk);
        if (chunkEntry != null) {
            TickThread thread = chunkEntry.thread;
            var chunks = threadChunkMap.get(thread);
            if (chunks != null) {
                chunks.remove(chunkEntry);
            }
            chunkEntryMap.remove(chunk);
        }
        this.chunks.remove(chunk);
    }

    private void processRemovedEntities() {
        if (removedEntities.isEmpty())
            return;
        for (Entity entity : removedEntities) {
            var acquirableEntity = entity.getAcquirable();
            ChunkEntry chunkEntry = acquirableEntity.getHandler().getChunkEntry();
            // Remove from list
            if (chunkEntry != null) {
                chunkEntry.entities.remove(entity);
            }
        }
        this.removedEntities.clear();
    }

    private void processUpdatedEntities() {
        if (updatableEntities.isEmpty())
            return;
        for (Entity entity : updatableEntities) {
            var acquirableEntity = entity.getAcquirable();
            ChunkEntry handlerChunkEntry = acquirableEntity.getHandler().getChunkEntry();
            // Remove from previous list
            if (handlerChunkEntry != null) {
                handlerChunkEntry.entities.remove(entity);
            }
            // Add to new list
            ChunkEntry chunkEntry = chunkEntryMap.get(entity.getChunk());
            if (chunkEntry != null) {
                chunkEntry.entities.add(entity);
                acquirableEntity.getHandler().refreshChunkEntry(chunkEntry);
            }
        }
        this.updatableEntities.clear();
    }

    /**
     * Defines how often chunks thread should be refreshed.
     */
    public enum RefreshType {
        /**
         * Chunk thread is constant after being defined.
         */
        NEVER,
        /**
         * Chunk thread should be recomputed as often as possible.
         */
        CONSTANT,
        /**
         * Chunk thread should be recomputed, but not continuously.
         */
        RARELY
    }

    public static class ChunkEntry {
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
