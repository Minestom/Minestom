package net.minestom.server.thread;

import net.minestom.server.UpdateManager;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * Used to link chunks into multiple groups.
 * Then executed into a thread pool.
 * <p>
 * You can change the current thread provider by calling {@link UpdateManager#setThreadProvider(ThreadProvider)}.
 */
public abstract class ThreadProvider {

    private final List<BatchThread> threads;

    private final Map<Integer, List<ChunkEntry>> threadChunkMap = new HashMap<>();
    private final Map<Chunk, ChunkEntry> chunkEntryMap = new HashMap<>();

    private final ArrayDeque<Chunk> batchHandlers = new ArrayDeque<>();

    public ThreadProvider(int threadCount) {
        this.threads = new ArrayList<>(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final BatchThread.BatchRunnable batchRunnable = new BatchThread.BatchRunnable();
            final BatchThread batchThread = new BatchThread(batchRunnable, i);
            this.threads.add(batchThread);

            batchThread.start();
        }
    }

    public void onInstanceCreate(@NotNull Instance instance) {
        instance.getChunks().forEach(this::addChunk);
    }

    public void onInstanceDelete(@NotNull Instance instance) {
        instance.getChunks().forEach(this::removeChunk);
    }

    public void onChunkLoad(Chunk chunk) {
        addChunk(chunk);
    }

    public void onChunkUnload(Chunk chunk) {
        removeChunk(chunk);
    }

    /**
     * Performs a server tick for all chunks based on their linked thread.
     *
     * @param chunk the chunk
     */
    public abstract int findThread(@NotNull Chunk chunk);

    protected void addChunk(Chunk chunk) {
        int thread = findThread(chunk);
        var chunks = threadChunkMap.computeIfAbsent(thread, ArrayList::new);

        ChunkEntry chunkEntry = new ChunkEntry(thread, chunk);
        chunks.add(chunkEntry);

        chunkEntryMap.put(chunk, chunkEntry);
    }

    protected void removeChunk(Chunk chunk) {
        ChunkEntry chunkEntry = chunkEntryMap.get(chunk);
        if (chunkEntry != null) {
            int threadId = chunkEntry.threadId;
            var chunks = threadChunkMap.get(threadId);
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
    public @NotNull CountDownLatch update(long time) {
        CountDownLatch countDownLatch = new CountDownLatch(threads.size());
        for (BatchThread thread : threads) {
            final int id = threads.indexOf(thread);
            if (id == -1) {
                countDownLatch.countDown();
                continue;
            }

            final var chunkEntries = threadChunkMap.get(id);
            if (chunkEntries == null) {
                countDownLatch.countDown();
                continue;
            }

            // Cache chunk entities
            Map<Chunk, List<Entity>> chunkListMap = new HashMap<>(chunkEntries.size());
            for (ChunkEntry chunkEntry : chunkEntries) {
                var chunk = chunkEntry.chunk;
                var entities = new ArrayList<>(chunk.getInstance().getChunkEntities(chunk));
                chunkListMap.put(chunk, entities);
            }

            // Execute tick
            thread.getMainRunnable().startTick(countDownLatch, () -> {
                chunkListMap.forEach((chunk, entities) -> {
                    chunk.tick(time);
                    entities.forEach(entity -> {
                        entity.tick(time);
                    });
                });
            });
        }
        return countDownLatch;
    }


    public void shutdown() {
        this.threads.forEach(BatchThread::shutdown);
    }

    @NotNull
    public List<BatchThread> getThreads() {
        return threads;
    }

    private static class ChunkEntry {
        private final int threadId;
        private final Chunk chunk;

        private ChunkEntry(int threadId, Chunk chunk) {
            this.threadId = threadId;
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