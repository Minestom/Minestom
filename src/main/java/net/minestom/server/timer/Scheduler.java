package net.minestom.server.timer;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.Supplier;

public sealed interface Scheduler permits SchedulerImpl, SchedulerManager {
    /**
     * Process scheduled tasks based on time.
     * <p>
     * Can be used to increase scheduling precision.
     */
    void process();

    /**
     * Advance 1 tick and call {@link #process()}.
     */
    void processTick();

    @NotNull OwnedTask submit(@NotNull Supplier<TaskSchedule> task,
                              @NotNull ExecutionType executionType);

    @NotNull OwnedTask submitAfter(@NotNull TaskSchedule schedule,
                                   @NotNull Supplier<TaskSchedule> task,
                                   @NotNull ExecutionType executionType);

    default @NotNull TaskBuilder buildTask(@NotNull Runnable task) {
        return new TaskBuilder(this, task);
    }

    @NotNull Collection<@NotNull OwnedTask> scheduledTasks();

    static @NotNull Scheduler newScheduler() {
        return new SchedulerImpl();
    }
}
