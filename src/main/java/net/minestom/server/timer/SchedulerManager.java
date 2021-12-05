package net.minestom.server.timer;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.Supplier;

public final class SchedulerManager implements Scheduler {
    private final Scheduler scheduler = Scheduler.newScheduler();

    @Override
    public void process() {
        this.scheduler.process();
    }

    @Override
    public void processTick() {
        this.scheduler.processTick();
    }

    @Override
    public @NotNull Task submit(@NotNull Supplier<TaskSchedule> task,
                                @NotNull ExecutionType executionType) {
        return scheduler.submit(task, executionType);
    }

    @Override
    public @NotNull Task submitAfter(@NotNull TaskSchedule schedule,
                                     @NotNull Supplier<TaskSchedule> task,
                                     @NotNull ExecutionType executionType) {
        return scheduler.submitAfter(schedule, task, executionType);
    }

    @Override
    public @NotNull Collection<@NotNull Task> scheduledTasks() {
        return scheduler.scheduledTasks();
    }

    public void shutdown() {
        // TODO
    }

    public Task buildShutdownTask(Runnable runnable) {
        // TODO
        return null;
    }
}
