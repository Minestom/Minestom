package net.minestom.server.thread;

import net.minestom.server.instance.Instance;
import net.minestom.server.thread.batch.BatchHandler;
import net.minestom.server.thread.batch.BatchSetupHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Used to link chunks into multiple groups.
 * Then executed into a thread pool.
 * <p>
 * You can change the current thread provider by calling {@link net.minestom.server.UpdateManager#setThreadProvider(ThreadProvider)}.
 */
public abstract class ThreadProvider {

    private List<BatchThread> threads;

    public ThreadProvider(int threadCount) {
        this.threads = new ArrayList<>(threadCount);

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
     * @param chunkX   the chunk X
     * @param chunkZ   the chunk Z
     */
    public abstract void onChunkLoad(@NotNull Instance instance, int chunkX, int chunkZ);

    /**
     * Called when a chunk is unloaded.
     *
     * @param instance the instance of the chunk
     * @param chunkX   the chunk X
     * @param chunkZ   the chunk Z
     */
    public abstract void onChunkUnload(@NotNull Instance instance, int chunkX, int chunkZ);

    /**
     * Performs a server tick for all chunks based on their linked thread.
     *
     * @param time the update time in milliseconds
     */
    public abstract void update(long time);

    public void createBatch(@NotNull Consumer<BatchHandler> consumer, long time) {
        BatchSetupHandler batchSetupHandler = new BatchSetupHandler();

        consumer.accept(batchSetupHandler);
        batchSetupHandler.pushTask(threads, time);
    }

    public void notifyThreads() {
        for (BatchThread thread : threads) {
            final BatchThread.BatchRunnable runnable = thread.getMainRunnable();
            synchronized (runnable) {
                runnable.notifyAll();
            }
        }
    }

}
