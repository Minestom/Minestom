package fr.themode.minestom.io;

import fr.themode.minestom.MinecraftServer;
import fr.themode.minestom.utils.thread.MinestomThread;

import java.util.concurrent.ExecutorService;

public class IOManager {

    private static final ExecutorService IO_POOL = new MinestomThread(MinecraftServer.THREAD_COUNT_IO, MinecraftServer.THREAD_NAME_IO);

    public static void submit(Runnable runnable) {
        IO_POOL.execute(runnable);
    }

}
