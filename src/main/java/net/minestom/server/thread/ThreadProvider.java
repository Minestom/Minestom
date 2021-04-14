package net.minestom.server.thread;

import net.minestom.server.UpdateManager;
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

    private final Map<Integer, List<Chunk>> threadChunkMap = new HashMap<>();
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
        chunks.add(chunk);
    }

    protected void removeChunk(Chunk chunk) {
        int thread = findThread(chunk);
        var chunks = threadChunkMap.get(thread);
        if (chunks != null) {
            chunks.remove(chunk);
        }
    }

    /**
     * Prepares the update.
     *
     * @param time the tick time in milliseconds
     */
    public void prepareUpdate(long time) {
        this.threadChunkMap.forEach((threadId, chunks) -> {
            BatchThread thread = threads.get(threadId);
            var chunksCopy = new ArrayList<>(chunks);
            thread.addRunnable(() -> {
                for (Chunk chunk : chunksCopy) {
                    chunk.tick(time);
                    chunk.getInstance().getChunkEntities(chunk).forEach(entity -> {
                        entity.tick(time);
                    });
                }
            });
        });
    }

    @NotNull
    public CountDownLatch notifyThreads() {
        CountDownLatch countDownLatch = new CountDownLatch(threads.size());
        for (BatchThread thread : threads) {
            final BatchThread.BatchRunnable runnable = thread.getMainRunnable();
            runnable.startTick(countDownLatch);
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
}