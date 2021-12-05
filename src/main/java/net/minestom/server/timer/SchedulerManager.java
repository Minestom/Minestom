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
    public @NotNull OwnedTask submit(@NotNull Supplier<TaskSchedule> task,
                                     @NotNull ExecutionType executionType) {
        return scheduler.submit(task, executionType);
    }

    @Override
    public @NotNull OwnedTask submitAfter(@NotNull TaskSchedule schedule,
                                          @NotNull Supplier<TaskSchedule> task,
                                          @NotNull ExecutionType executionType) {
        return scheduler.submitAfter(schedule, task, executionType);
    }

    @Override
    public @NotNull Collection<@NotNull OwnedTask> scheduledTasks() {
        return scheduler.scheduledTasks();
    }

    public void shutdown() {
        // TODO
    }

    public OwnedTask buildShutdownTask(Runnable runnable) {
        // TODO
        return null;
    }
}
