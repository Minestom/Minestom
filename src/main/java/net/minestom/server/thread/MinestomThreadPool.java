package net.minestom.server.thread;

import org.jetbrains.annotations.ApiStatus;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@ApiStatus.Internal
public class MinestomThreadPool extends ThreadPoolExecutor {

    private static final Set<MinestomThreadPool> executors = new CopyOnWriteArraySet<>();

    /**
     * Creates a non-local thread pool executor
     *
     * @param nThreads the number of threads
     * @param name     the name of the thread pool
     */
    public MinestomThreadPool(int nThreads, String name) {
        this(nThreads, name, false);
    }

    /**
     * @param nThreads the number of threads
     * @param name     the name of the thread pool
     * @param local    set to true if this executor is only used inside a method and should *not* be kept in the internal list of executors
     */
    public MinestomThreadPool(int nThreads, String name, boolean local) {
        super(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), r -> {
            Thread thread = new MinestomThread(r);
            thread.setDaemon(true);
            thread.setName(thread.getName().replace("Thread", name));
            return thread;
        });
        if (!local) {
            MinestomThreadPool.executors.add(this);
        }
    }

    /**
     * Shutdown all the thread pools
     */
    public static void shutdownAll() {
        executors.forEach(MinestomThreadPool::shutdownNow);
    }
}
