package net.minestom.server;

import net.minestom.server.thread.TickSchedulerThread;
import net.minestom.server.timer.TaskSchedule;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class ServerProcessTest {

    @Test
    public void init() {
        AtomicReference<ServerProcess> process = new AtomicReference<>();
        assertDoesNotThrow(() -> process.set(MinecraftServer.updateProcess()));
        assertDoesNotThrow(() -> process.get().start(new InetSocketAddress("localhost", 25565)));
        assertThrows(Exception.class, () -> process.get().start(new InetSocketAddress("localhost", 25566)));
        assertDoesNotThrow(() -> process.get().stop());
    }

    @Test
    public void tick() {
        var process = MinecraftServer.updateProcess();
        process.start(new InetSocketAddress("localhost", 25567));
        var ticker = process.ticker();
        assertDoesNotThrow(() -> ticker.tick(System.currentTimeMillis()));
        assertDoesNotThrow(process::stop);
    }

    @Test
    public void tickScheduling() {
        var process = MinecraftServer.updateProcess();
        process.start(new InetSocketAddress("localhost", 25568));
        AtomicLong time = new AtomicLong();
        AtomicInteger counter = new AtomicInteger();

        CountDownLatch latch = new CountDownLatch(1);
        process.scheduler().scheduleTask(() -> {
            long timeNow = System.currentTimeMillis();
            long lastTime = time.get();
            if (lastTime != 0) {
                long diff = timeNow - lastTime;
                assertEquals(MinecraftServer.TICK_MS, diff, 5);
            }
            time.set(timeNow);
            if (counter.incrementAndGet() == 10) {
                process.stop();
                latch.countDown();
            }
        }, TaskSchedule.nextTick(), TaskSchedule.nextTick());
        new TickSchedulerThread(process).start();
        assertDoesNotThrow(() -> latch.await());
    }
}
