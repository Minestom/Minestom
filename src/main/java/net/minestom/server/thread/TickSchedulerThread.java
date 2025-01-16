package net.minestom.server.thread;

import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerFlag;
import net.minestom.server.ServerProcess;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class TickSchedulerThread extends MinestomThread {
    private static final long TICK_TIME_NANOS = 1_000_000_000L / ServerFlag.SERVER_TICKS_PER_SECOND;
    // Windows has an issue with periodically being unable to sleep for < ~16ms at a time
    private static final long SLEEP_THRESHOLD = System.getProperty("os.name", "")
            .toLowerCase().startsWith("windows") ? 17 : 2;

    private final ServerProcess serverProcess;

    public TickSchedulerThread(ServerProcess serverProcess) {
        super(MinecraftServer.THREAD_NAME_TICK_SCHEDULER);
        this.serverProcess = serverProcess;
    }

    @Override
    public void run() {
        long ticks = 0;
        long baseTime = System.nanoTime();
        while (serverProcess.isAlive()) {
            final long tickStart = System.nanoTime();
            try {
                serverProcess.ticker().tick(tickStart);
            } catch (Throwable e) {
                serverProcess.exception().handleException(e);
            }

            ticks++;
            long nextTickTime = baseTime + ticks * TICK_TIME_NANOS;
            waitUntilNextTick(nextTickTime);
            // Check if the server can not keep up with the tickrate
            // if it gets too far behind, reset the fireTicks & baseTime
            // to avoid running too many fireTicks at once
            if (System.nanoTime() > nextTickTime + TICK_TIME_NANOS * ServerFlag.SERVER_MAX_TICK_CATCH_UP) {
                baseTime = System.nanoTime();
                ticks = 0;
            }
        }
    }

    private void waitUntilNextTick(long nextTickTimeNanos) {
        long currentTime;
        while ((currentTime = System.nanoTime()) < nextTickTimeNanos) {
            long remainingTime = nextTickTimeNanos - currentTime;
            // Sleep less the closer we are to the next tick
            long remainingMilliseconds = remainingTime / 1_000_000L;
            if (remainingMilliseconds >= SLEEP_THRESHOLD) {
                sleepThread(remainingMilliseconds / 2);
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
