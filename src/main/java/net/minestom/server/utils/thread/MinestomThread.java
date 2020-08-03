package net.minestom.server.utils.thread;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MinestomThread extends ThreadPoolExecutor {

    private static final List<MinestomThread> executors = new LinkedList<>();

    /**
     * Creates a non-local thread pool executor
     *
     * @param nThreads the number of threads
     * @param name     the name of the thread pool
     */
    public MinestomThread(int nThreads, String name) {
        this(nThreads, name, false);
    }

    /**
     * @param nThreads the number of threads
     * @param name     the name of the thread pool
     * @param local    set to true if this executor is only used inside a method and should *not* be kept in the internal list of executors
     */
    public MinestomThread(int nThreads, String name, boolean local) {
        super(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName(thread.getName().replace("Thread", name));
            return thread;
        });
        if (!local) {
            executors.add(this);
        }
    }

    /**
     * Shutdown all the thread pools
     */
    public static void shutdownAll() {
        executors.forEach(MinestomThread::shutdownNow);
    }
}
