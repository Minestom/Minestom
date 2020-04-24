package net.minestom.server.utils.thread;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MinestomThread extends ThreadPoolExecutor {

    public MinestomThread(int nThreads, String name) {
        super(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), r -> {
            Thread thread = new Thread(r);
            thread.setName(thread.getName().replace("Thread", name));
            return thread;
        });
    }
}
