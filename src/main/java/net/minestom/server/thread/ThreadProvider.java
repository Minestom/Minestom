package net.minestom.server.thread;

import net.minestom.server.UpdateManager;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.thread.batch.BatchHandler;
import net.minestom.server.thread.batch.BatchSetupHandler;
import net.minestom.server.utils.time.Cooldown;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.utils.time.UpdateOption;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

/**
 * Used to link chunks into multiple groups.
 * Then executed into a thread pool.
 * <p>
 * You can change the current thread provider by calling {@link UpdateManager#setThreadProvider(ThreadProvider)}.
 */
public abstract class ThreadProvider {

    private final Set<BatchThread> threads;

    private final List<BatchSetupHandler> batchHandlers = new ArrayList<>();

    private UpdateOption batchesRefreshCooldown;
    private long lastBatchRefreshTime;

    public ThreadProvider(int threadCount) {
        this.threads = new HashSet<>(threadCount);
        this.batchesRefreshCooldown = new UpdateOption(500, TimeUnit.MILLISECOND);

        for (int i = 0; i < threadCount; i++) {
            final BatchThread.BatchRunnable batchRunnable = new BatchThread.BatchRunnable();
            final BatchThread batchThread = new BatchThread(batchRunnable, i);
            this.threads.add(batchThread);

            batchThread.start();
        }
    }

    /**
     * Called when an {@link Instance} is registered.
     *
     * @param instance the newly create {@link Instance}
     */
    public abstract void onInstanceCreate(@NotNull Instance instance);

    /**
     * Called when an {@link Instance} is unregistered.
     *
     * @param instance the deleted {@link Instance}
     */
    public abstract void onInstanceDelete(@NotNull Instance instance);

    /**
     * Called when a chunk is loaded.
     * <p>
     * Be aware that this is possible for an instance to load chunks before being registered.
     *
     * @param instance the instance of the chunk
     * @param chunk    the chunk
     */
    public abstract void onChunkLoad(@NotNull Instance instance, Chunk chunk);

    /**
     * Called when a chunk is unloaded.
     *
     * @param instance the instance of the chunk
     * @param chunk    the chunk
     */
    public abstract void onChunkUnload(@NotNull Instance instance, Chunk chunk);

    /**
     * Performs a server tick for all chunks based on their linked thread.
     *
     * @param time the update time in milliseconds
     */
    public abstract void update(long time);

    public void createBatch(@NotNull Consumer<BatchHandler> consumer, long time) {
        BatchSetupHandler batchSetupHandler = new BatchSetupHandler();

        consumer.accept(batchSetupHandler);

        this.batchHandlers.add(batchSetupHandler);

        batchSetupHandler.pushTask(threads, time);
    }

    /**
     * Prepares the update.
     * <p>
     * {@link #update(long)} is called based on its cooldown to limit the overhead.
     * The cooldown can be modified using {@link #setBatchesRefreshCooldown(UpdateOption)}.
     *
     * @param time the tick time in milliseconds
     */
    public void prepareUpdate(long time) {
        // Verify if the batches should be updated
        if (batchesRefreshCooldown == null ||
                !Cooldown.hasCooldown(time, lastBatchRefreshTime, batchesRefreshCooldown)) {
            this.lastBatchRefreshTime = time;
            this.batchHandlers.clear();
            update(time);
        } else {
            // Push the tasks
            for (BatchSetupHandler batchHandler : batchHandlers) {
                batchHandler.pushTask(threads, time);
            }
        }
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

    public void cleanup() {
        for (BatchThread thread : threads) {
            thread.setCost(0);
        }
    }

    public void shutdown() {
        this.threads.forEach(BatchThread::shutdown);
    }

    @NotNull
    public Set<BatchThread> getThreads() {
        return threads;
    }

    @Nullable
    public UpdateOption getBatchesRefreshCooldown() {
        return batchesRefreshCooldown;
    }

    public void setBatchesRefreshCooldown(@Nullable UpdateOption batchesRefreshCooldown) {
        this.batchesRefreshCooldown = batchesRefreshCooldown;
    }
}