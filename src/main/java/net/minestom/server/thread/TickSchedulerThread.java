package net.minestom.server.thread;

import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerProcess;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class TickSchedulerThread extends MinestomThread {
    // Windows has an issue with periodically being unable to sleep for < ~16ms at a time
    private static final long SLEEP_THRESHOLD = System.getProperty("os.name", "")
            .toLowerCase().startsWith("windows") ? 17 : 5;

    private final ServerProcess serverProcess;

    public TickSchedulerThread(ServerProcess serverProcess) {
        super(MinecraftServer.THREAD_NAME_TICK_SCHEDULER);
        this.serverProcess = serverProcess;
    }

    @Override
    public void run() {
        while (serverProcess.isAlive()) {
            final long tickStart = System.currentTimeMillis();
            try {
                serverProcess.ticker().tick(tickStart);
            } catch (Exception e) {
                serverProcess.exception().handleException(e);
            }
            long tickEnd = System.currentTimeMillis();
            long timeElapsed = tickEnd - tickStart;
            waitUntilNextTick(tickEnd + MinecraftServer.TICK_MS - timeElapsed);
        }
    }

    private void waitUntilNextTick(long nextTickTimeMillis) {
        long currentTime;
        while ((currentTime = System.currentTimeMillis()) < nextTickTimeMillis) {
            long remainingTime = nextTickTimeMillis - currentTime;
            // Sleep less the closer we are to the next tick
            if (remainingTime >= SLEEP_THRESHOLD) {
                sleepThread(remainingTime / 2);
            }
        }
    }

    private void sleepThread(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            serverProcess.exception().handleException(e);
        }
    }
}
