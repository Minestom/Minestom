package net.minestom.server.timer;

import net.minestom.server.MinecraftServer;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class TestScheduler {

    @Test
    public void tickTask() {
        Scheduler scheduler = Scheduler.newScheduler();
        AtomicBoolean result = new AtomicBoolean(false);
        Task task = scheduler.scheduleNextTick(() -> result.set(true));
        assertEquals(task.executionType(), ExecutionType.SYNC, "Tasks default execution type should be sync");

        assertFalse(result.get(), "Tick task should not be executed after scheduling");
        scheduler.process();
        assertFalse(result.get(), "Tick task should not be executed after process");
        scheduler.processTick();
        assertTrue(result.get(), "Tick task must be executed after tick process");
        assertFalse(task.isAlive(), "Tick task should be cancelled after execution");
    }

    @Test
    public void durationTask() throws InterruptedException {
        Scheduler scheduler = Scheduler.newScheduler();
        AtomicBoolean result = new AtomicBoolean(false);
        scheduler.buildTask(() -> result.set(true))
                .delay(TaskSchedule.seconds(1))
                .schedule();
        Thread.sleep(100);
        scheduler.process();
        assertFalse(result.get(), "900ms remaining");
        Thread.sleep(1200);
        scheduler.process();
        assertTrue(result.get(), "Tick task must be executed after 1 second");
    }

    @Test
    public void immediateTask() {
        Scheduler scheduler = Scheduler.newScheduler();
        AtomicBoolean result = new AtomicBoolean(false);
        scheduler.scheduleNextProcess(() -> result.set(true));
        assertFalse(result.get());
        scheduler.process();
        assertTrue(result.get());

        result.set(false);
        scheduler.process();
        assertFalse(result.get());
    }

    @Test
    public void cancelTask() {
        Scheduler scheduler = Scheduler.newScheduler();
        AtomicBoolean result = new AtomicBoolean(false);
        var task = scheduler.buildTask(() -> result.set(true))
                .schedule();
        assertTrue(task.isAlive(), "Task should still be alive");
        task.cancel();
        assertFalse(task.isAlive(), "Task should not be alive anymore");
        scheduler.process();
        assertFalse(result.get(), "Task should be cancelled");
    }

    @Test
    public void cancelAsyncDelayedTask() throws InterruptedException {
        Scheduler scheduler = Scheduler.newScheduler();
        AtomicBoolean result = new AtomicBoolean(false);
        var task = scheduler.buildTask(() -> result.set(true))
                .delay(Duration.ofMillis(1))
                .executionType(ExecutionType.ASYNC)
                .schedule();
        assertTrue(task.isAlive(), "Task should still be alive");
        task.cancel();
        assertFalse(task.isAlive(), "Task should not be alive anymore");
        scheduler.process();
        Thread.sleep(10L);
        assertFalse(result.get(), "Task should be cancelled");
    }

    @Test
    public void parkTask() {
        Scheduler scheduler = Scheduler.newScheduler();
        // Ignored parked task
        scheduler.buildTask(() -> fail("This parked task should never be executed"))
                .executionType(ExecutionType.SYNC)
                .delay(TaskSchedule.park())
                .schedule();

        // Unpark task
        AtomicBoolean result = new AtomicBoolean(false);
        var task = scheduler.buildTask(() -> result.set(true))
                .delay(TaskSchedule.park())
                .schedule();
        assertTrue(task.isParked());
        assertFalse(result.get(), "Task hasn't been unparked yet");
        task.unpark();
        assertFalse(task.isParked());
        assertFalse(result.get(), "Tasks must be processed first");
        scheduler.process();
        assertFalse(task.isParked());
        assertTrue(result.get(), "Parked task should be executed");
    }

    @Test
    public void futureTask() {
        Scheduler scheduler = Scheduler.newScheduler();
        CompletableFuture<Void> future = new CompletableFuture<>();
        AtomicBoolean result = new AtomicBoolean(false);
        scheduler.buildTask(() -> result.set(true))
                .delay(TaskSchedule.future(future))
                .schedule();
        assertFalse(result.get(), "Future is not completed yet");
        future.complete(null);
        assertFalse(result.get(), "Tasks must be processed first");
        scheduler.process();
        assertTrue(result.get(), "Future should be completed");
    }

    @Test
    public void asyncTask() throws InterruptedException {
        final Thread currentThread = Thread.currentThread();
        Scheduler scheduler = Scheduler.newScheduler();
        AtomicBoolean result = new AtomicBoolean(false);
        scheduler.buildTask(() -> {
                    assertNotEquals(currentThread, Thread.currentThread(),
                            "Task should be executed in a different thread");
                    result.set(true);
                })
                .executionType(ExecutionType.ASYNC)
                .schedule();
        assertFalse(result.get(), "Async task should only be executed after process()");
        scheduler.process();
        Thread.sleep(250);
        assertTrue(result.get(), "Async task didn't get executed");
    }

    @Test
    public void exceptionTask() {
        MinecraftServer.init();
        Scheduler scheduler = Scheduler.newScheduler();
        scheduler.scheduleNextTick(() -> {
            throw new RuntimeException("Test exception");
        });

        // This is a bit of a weird use case. I dont want this test to depend on the order the scheduler executes in
        // so this is a guess that the first one wont be before all 100 of the ones scheduled below.
        // Not great, but should be fine anyway.
        AtomicInteger executed = new AtomicInteger(0);
        for (int i = 0; i < 100; i++) {
            scheduler.scheduleNextTick(executed::incrementAndGet);
        }

        assertDoesNotThrow(scheduler::processTick);
        assertEquals(100, executed.get());
    }
}
