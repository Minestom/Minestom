package net.minestom.server.thread;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.lock.Acquirable;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * Used to link chunks into multiple groups.
 * Then executed into a thread pool.
 */
public abstract class ThreadProvider {

    private final List<BatchThread> threads;

    private final Map<BatchThread, Set<ChunkEntry>> threadChunkMap = new HashMap<>();
    private final Map<Chunk, ChunkEntry> chunkEntryMap = new HashMap<>();
    private final Set<Entity> removedEntities = ConcurrentHashMap.newKeySet();

    public ThreadProvider(int threadCount) {
        this.threads = new ArrayList<>(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final BatchThread.BatchRunnable batchRunnable = new BatchThread.BatchRunnable();
            final BatchThread batchThread = new BatchThread(batchRunnable, i);
            this.threads.add(batchThread);

            batchThread.start();
        }
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

    protected void addChunk(Chunk chunk) {
        int threadId = findThread(chunk);
        BatchThread thread = threads.get(threadId);
        var chunks = threadChunkMap.computeIfAbsent(thread, batchThread -> ConcurrentHashMap.newKeySet());

        ChunkEntry chunkEntry = new ChunkEntry(thread, chunk);
        chunks.add(chunkEntry);

        chunkEntryMap.put(chunk, chunkEntry);
    }

    protected void removeChunk(Chunk chunk) {
        ChunkEntry chunkEntry = chunkEntryMap.get(chunk);
        if (chunkEntry != null) {
            BatchThread thread = chunkEntry.thread;
            var chunks = threadChunkMap.get(thread);
            if (chunks != null) {
                chunks.remove(chunkEntry);
            }
            chunkEntryMap.remove(chunk);
        }
    }

    /**
     * Prepares the update.
     *
     * @param time the tick time in milliseconds
     */
    public synchronized @NotNull CountDownLatch update(long time) {
        CountDownLatch countDownLatch = new CountDownLatch(threads.size());
        for (BatchThread thread : threads) {
            final var chunkEntries = threadChunkMap.get(thread);
            if (chunkEntries == null) {
                countDownLatch.countDown();
                continue;
            }

            // Execute tick
            thread.getMainRunnable().startTick(countDownLatch, () -> {
                final var entitiesList = chunkEntries.stream().map(chunkEntry -> chunkEntry.entities).collect(Collectors.toList());
                final var entities = entitiesList.stream()
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
                Acquirable.refreshEntities(Collections.unmodifiableList(entities));
                chunkEntries.forEach(chunkEntry -> {
                    chunkEntry.chunk.tick(time);
                    chunkEntry.entities.forEach(entity -> {
                        entity.tick(time);
                    });
                });
            });
        }
        return countDownLatch;
    }

    public synchronized void refreshThreads() {

        // Clear removed entities
        for (Entity entity : removedEntities) {
            Acquirable<Entity> acquirable = entity.getAcquiredElement();
            Chunk batchChunk = acquirable.getHandler().getBatchChunk();

            // Remove from list
            {
                ChunkEntry chunkEntry = chunkEntryMap.get(batchChunk);
                if (chunkEntry != null) {
                    chunkEntry.entities.remove(entity);
                }
            }
        }
        this.removedEntities.clear();

        // Update as many entities as possible
        // TODO: incremental update instead of full
        for (Instance instance : MinecraftServer.getInstanceManager().getInstances()) {
            var entities = instance.getEntities();
            for (Entity entity : entities) {
                Acquirable<Entity> acquirable = entity.getAcquiredElement();
                Chunk batchChunk = acquirable.getHandler().getBatchChunk();
                Chunk entityChunk = entity.getChunk();
                if (!Objects.equals(batchChunk, entityChunk)) {
                    // Entity is possibly not in the correct thread

                    // Remove from previous list
                    {
                        ChunkEntry chunkEntry = chunkEntryMap.get(batchChunk);
                        if (chunkEntry != null) {
                            chunkEntry.entities.remove(entity);
                        }
                    }

                    // Add to new list
                    {
                        ChunkEntry chunkEntry = chunkEntryMap.get(entityChunk);
                        if (chunkEntry != null) {
                            chunkEntry.entities.add(entity);
                            acquirable.getHandler().refreshBatchInfo(chunkEntry.thread, chunkEntry.chunk);
                        }
                    }
                }
            }
        }
    }

    public void removeEntity(Entity entity) {
        this.removedEntities.add(entity);
    }

    public void shutdown() {
        this.threads.forEach(BatchThread::shutdown);
    }

    @NotNull
    public List<BatchThread> getThreads() {
        return threads;
    }

    private static class ChunkEntry {
        private final BatchThread thread;
        private final Chunk chunk;
        private final List<Entity> entities = new ArrayList<>();

        private ChunkEntry(BatchThread thread, Chunk chunk) {
            this.thread = thread;
            this.chunk = chunk;
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