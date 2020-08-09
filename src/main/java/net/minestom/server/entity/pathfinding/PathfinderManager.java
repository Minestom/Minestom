package net.minestom.server.entity.pathfinding;

import net.minestom.server.MinecraftServer;
import net.minestom.server.utils.thread.MinestomThread;

import java.util.concurrent.ExecutorService;

public final class PathfinderManager {

    private ExecutorService pathfinderPool = new MinestomThread(MinecraftServer.THREAD_COUNT_PATHFINDER, MinecraftServer.THREAD_NAME_PATHFINDER);

    public ExecutorService getPool() {
        return pathfinderPool;
    }

}
