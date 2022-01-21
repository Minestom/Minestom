package net.minestom.server.thread;

import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerProcess;
import org.jetbrains.annotations.ApiStatus;

import java.util.concurrent.locks.LockSupport;

@ApiStatus.Internal
public final class TickSchedulerThread extends MinestomThread {
    private final ServerProcess serverProcess;

    public TickSchedulerThread(ServerProcess serverProcess) {
        super(MinecraftServer.THREAD_NAME_TICK_SCHEDULER);
        this.serverProcess = serverProcess;
    }

    @Override
    public void run() {
        final long tickNs = (long) (MinecraftServer.TICK_MS * 1e6);
        while (serverProcess.isAlive()) {
            final long tickStart = System.nanoTime();
            try {
                serverProcess.ticker().tick(tickStart);
            } catch (Exception e) {
                serverProcess.exception().handleException(e);
            }
            final long tickTime = System.nanoTime() - tickStart;
            LockSupport.parkNanos(tickNs - tickTime);
        }
    }
}
