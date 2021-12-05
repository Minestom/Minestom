package net.minestom.server.timer;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.function.Supplier;

public final class TaskBuilder {
    private final Scheduler scheduler;
    private final Runnable runnable;
    private ExecutionType executionType = ExecutionType.SYNC;
    private TaskSchedule startSchedule;
    private TaskSchedule stepSchedule;

    TaskBuilder(Scheduler scheduler, Runnable runnable) {
        this.scheduler = scheduler;
        this.runnable = runnable;
    }

    public @NotNull TaskBuilder executionType(@NotNull ExecutionType executionType) {
        this.executionType = executionType;
        return this;
    }

    public @NotNull TaskBuilder startSchedule(@NotNull TaskSchedule schedule) {
        this.startSchedule = schedule;
        return this;
    }

    public @NotNull TaskBuilder stepSchedule(@NotNull TaskSchedule schedule) {
        this.stepSchedule = schedule;
        return this;
    }

    public @NotNull TaskBuilder delay(@NotNull Duration duration) {
        return startSchedule(TaskSchedule.duration(duration));
    }

    public @NotNull TaskBuilder delay(long time, @NotNull TemporalUnit unit) {
        return delay(Duration.of(time, unit));
    }

    public @NotNull TaskBuilder repeat(@NotNull Duration duration) {
        return stepSchedule(TaskSchedule.duration(duration));
    }

    public @NotNull TaskBuilder repeat(long time, @NotNull TemporalUnit unit) {
        return repeat(Duration.of(time, unit));
    }

    public @NotNull OwnedTask schedule() {
        final Supplier<TaskSchedule> supplier = () -> {
            runnable.run();
            return stepSchedule != null ? stepSchedule : TaskSchedule.stop();
        };
        return startSchedule != null ? scheduler.submitAfter(startSchedule, supplier, executionType) : scheduler.submit(supplier, executionType);
    }
}
