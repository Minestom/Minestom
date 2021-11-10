package net.minestom.server.utils.thread;

import net.minestom.server.thread.MinestomThreadPool;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Executor service which will always give the same thread to a given Runnable.
 * Uses <pre>Runnable#hashCode()</pre> to determine the thread to assign.
 */
public class ThreadBindingExecutor extends AbstractExecutorService {

    private MinestomThreadPool[] threadExecutors;

    /**
     * Creates a non-local thread-binding executor
     *
     * @param nThreads the number of threads
     * @param name     the name of the thread pool
     */
    public ThreadBindingExecutor(int nThreads, String name) {
        this(nThreads, name, false);
    }

    /**
     * @param nThreads the number of threads
     * @param name     the name of the thread pool
     * @param local    set to true if this executor is only used inside a method and should *not* be kept in the internal list of executors
     */
    public ThreadBindingExecutor(int nThreads, String name, boolean local) {
        threadExecutors = new MinestomThreadPool[nThreads];
        for (int i = 0; i < nThreads; i++) {
            threadExecutors[i] = new MinestomThreadPool(1, name, local);
        }
    }

    @Override
    public void shutdown() {
        for (MinestomThreadPool t : threadExecutors) {
            t.shutdown();
        }
    }

    @NotNull
    @Override
    public List<Runnable> shutdownNow() {
        List<Runnable> allTasks = new LinkedList<>();
        for (MinestomThreadPool t : threadExecutors) {
            allTasks.addAll(t.shutdownNow());
        }
        return allTasks;
    }

    @Override
    public boolean isShutdown() {
        for (MinestomThreadPool t : threadExecutors) {
            if(!t.isShutdown())
                return false;
        }
        return true;
    }

    @Override
    public boolean isTerminated() {
        for (MinestomThreadPool t : threadExecutors) {
            if(!t.isShutdown())
                return false;
        }
        return true;
    }

    @Override
    public boolean awaitTermination(long timeout, @NotNull TimeUnit unit) throws InterruptedException {
        boolean terminated = true;
        for (MinestomThreadPool t : threadExecutors) {
            terminated &= t.awaitTermination(timeout, unit);
        }
        return terminated;
    }

    @Override
    public void execute(@NotNull Runnable command) {
        int hash = command.hashCode();
        if(hash < 0) hash = -hash;
        int bucket = hash % threadExecutors.length;

        threadExecutors[bucket].execute(command);
    }
}
