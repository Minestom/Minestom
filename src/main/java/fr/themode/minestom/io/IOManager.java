package fr.themode.minestom.io;

import fr.themode.minestom.Main;
import fr.themode.minestom.utils.thread.MinestomThread;

import java.util.concurrent.ExecutorService;

public class IOManager {

    private static final ExecutorService IO_POOL = new MinestomThread(Main.THREAD_COUNT_IO, "Ms-IOPool");

    public static void submit(Runnable runnable) {
        IO_POOL.execute(runnable);
    }

}
