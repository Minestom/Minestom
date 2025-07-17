package net.minestom.server.timer;

import net.minestom.server.MinecraftServer;
import org.junit.jupiter.api.Test;

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
        assertEquals(task.executionType(), ExecutionType.TICK_START, "Tasks default execution type should be tick start");

        assertFalse(result.get(), "Tick task should not be executed after scheduling");
        scheduler.process();
        assertFalse(result.get(), "Tick task should not be executed after process");
        scheduler.processTickEnd();
        assertFalse(result.get(), "Tick task should not be executed after processTickEnd");
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
        scheduler.processTickEnd();
        assertFalse(result.get(), "processTickEnd should never execute immediate tasks unless it is of type TICK_END");
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
    public void parkTask() {
        Scheduler scheduler = Scheduler.newScheduler();
        // Ignored parked task
        scheduler.buildTask(() -> fail("This parked task should never be executed"))
                .executionType(ExecutionType.TICK_START)
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

    @Test
    public void scheduleEndOfTick() {
        Scheduler scheduler = Scheduler.newScheduler();
        AtomicBoolean result = new AtomicBoolean(false);
        scheduler.scheduleEndOfTick(() -> result.set(true));
        assertFalse(result.get(), "End of tick tasks should not be executed immediately upon submission");
        scheduler.processTick();
        assertFalse(result.get(), "End of tick tasks should not be executed by processTick()");
        scheduler.processTickEnd();
        assertTrue(result.get(), "scheduleEndOfTick(...) tasks should be executed after the next call to processTickEnd()");

        result.set(false);
        scheduler.scheduleEndOfTick(() -> result.set(true));
        scheduler.processTickEnd();
        assertTrue(result.get(), "scheduleEndOfTick(...) tasks should always execute on the very next processTickEnd()");
    }

    @Test
    public void delayedEndOfTick() {
        Scheduler scheduler = Scheduler.newScheduler();
        AtomicBoolean result = new AtomicBoolean(false);
        scheduler.buildTask(() -> result.set(true)).delay(TaskSchedule.tick(1))
                .executionType(ExecutionType.TICK_END).schedule();

        scheduler.processTickEnd(); scheduler.processTickEnd();
        assertFalse(result.get(), "processTickEnd() should not increment the scheduler's internal tick counter");
        scheduler.processTick();
        scheduler.processTickEnd();
        assertTrue(result.get(), "processTick() should increment the current tick counter processTickEnd() uses");
    }

    @Test
    public void repeatingEndOfTick() {
        Scheduler scheduler = Scheduler.newScheduler();
        AtomicInteger result = new AtomicInteger(0);
        Task task = scheduler.scheduleTask(result::getAndIncrement, TaskSchedule.immediate(), TaskSchedule.tick(1), ExecutionType.TICK_END);
        assertEquals(0, result.get(), "TICK_END tasks should not be executed immediately upon submission");
        scheduler.processTickEnd();
        assertEquals(1, result.get(), "processTickEnd() should always execute TaskSchedule.immediate() TICK_END tasks");
        scheduler.processTickEnd();
        assertEquals(1, result.get(), "task should not executed on processTickEnd() again until processTick() is called");
        scheduler.processTick();
        assertEquals(1, result.get(), "processTick() should never execute TICK_END tasks");
        scheduler.processTickEnd();
        assertEquals(2, result.get(), "processTickEnd() should execute this task");

        task.cancel();
        scheduler.processTick();
        scheduler.processTickEnd();
        assertEquals(2, result.get(), "this task should have been cancelled");
    }

    @Test
    public void durationEndOfTick() throws InterruptedException {
        Scheduler scheduler = Scheduler.newScheduler();
        AtomicBoolean result = new AtomicBoolean(false);
        scheduler.buildTask(() -> result.set(true))
                .delay(TaskSchedule.seconds(1))
                .executionType(ExecutionType.TICK_END)
                .schedule();
        Thread.sleep(100);
        scheduler.process();
        scheduler.processTickEnd();
        assertFalse(result.get(), "900ms remaining");
        Thread.sleep(1200);
        scheduler.process();
        assertFalse(result.get(), "process() should never execute TICK_END tasks");
        scheduler.processTickEnd();
        assertTrue(result.get(), "Tick end task must be executed after 1 second");
    }
}
