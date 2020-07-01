package net.minestom.server.utils.thread;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.SchedulerManager;
import net.minestom.server.timer.TaskRunnable;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MinestomThread extends ThreadPoolExecutor {

    private static final List<MinestomThread> executors = new LinkedList<>();

    public MinestomThread(int nThreads, String name) {
        super(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName(thread.getName().replace("Thread", name));
            return thread;
        });
        executors.add(this);
    }

    public static void shutdownAll() {
        executors.forEach(MinestomThread::shutdownNow);
    }
}
