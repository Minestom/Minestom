package net.minestom.server.timer;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * Represents a scheduler that will execute tasks with a precision based on its ticking rate.
 * If precision is important, consider using a JDK executor service or any third party library.
 * <p>
 * Tasks are by default executed in the caller thread.
 */
public sealed interface Scheduler permits SchedulerImpl, SchedulerManager {
    static @NotNull Scheduler newScheduler() {
        return new SchedulerImpl();
    }

    /**
     * Process scheduled tasks based on time to increase scheduling precision.
     * <p>
     * This method is not thread-safe.
     */
    void process();

    /**
     * Advance 1 tick and call {@link #process()}.
     * <p>
     * This method is not thread-safe.
     */
    void processTick();

    @NotNull Task submit(@NotNull Supplier<TaskSchedule> task,
                         @NotNull ExecutionType executionType);

    default @NotNull Task.Builder buildTask(@NotNull Runnable task) {
        return new Task.Builder(this, task);
    }

    @NotNull Collection<@NotNull Task> scheduledTasks();

    default @NotNull Task scheduleTask(@NotNull Runnable task,
                                       @NotNull TaskSchedule delay, @NotNull TaskSchedule repeat,
                                       @NotNull ExecutionType executionType) {
        return buildTask(task).delay(delay).repeat(repeat).executionType(executionType).schedule();
    }

    default @NotNull Task scheduleTask(@NotNull Runnable task,
                                       @NotNull TaskSchedule delay, @NotNull TaskSchedule repeat) {
        return scheduleTask(task, delay, repeat, ExecutionType.SYNC);
    }

    default @NotNull Task scheduleNextTick(@NotNull Runnable task, @NotNull ExecutionType executionType) {
        return buildTask(task).delay(TaskSchedule.nextTick()).executionType(executionType).schedule();
    }

    default @NotNull Task scheduleNextTick(@NotNull Runnable task) {
        return scheduleNextTick(task, ExecutionType.SYNC);
    }
}
