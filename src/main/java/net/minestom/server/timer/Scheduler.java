package net.minestom.server.timer;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * Represents a scheduler that will execute tasks with a precision based on its ticking rate.
 * If precision is important, consider using a JDK executor service or any third party library.
 * <p>
 * Tasks are by default executed in the caller thread.
 */
public sealed interface Scheduler extends Executor permits SchedulerImpl, SchedulerManager {
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

    /**
     * Execute tasks set to run at the end of this tick.
     * <p>
     * This method is not thread-safe.
     */
    void processTickEnd();

    /**
     * Submits a new task with custom scheduling logic.
     * <p>
     * This is the primitive method used by all scheduling shortcuts,
     * {@code task} is immediately executed in the caller thread to retrieve its scheduling state
     * and the task will stay alive as long as {@link TaskSchedule#stop()} is not returned (or {@link Task#cancel()} is called).
     *
     * @param task          the task to be directly executed in the caller thread
     * @param executionType the execution type
     * @return the created task
     */
    @NotNull Task submitTask(@NotNull Supplier<TaskSchedule> task, @NotNull ExecutionType executionType);

    default @NotNull Task submitTask(@NotNull Supplier<TaskSchedule> task) {
        return submitTask(task, ExecutionType.TICK_START);
    }

    default @NotNull Task.Builder buildTask(@NotNull Runnable task) {
        return new Task.Builder(this, task);
    }

    default @NotNull Task scheduleTask(@NotNull Runnable task,
                                       @NotNull TaskSchedule delay, @NotNull TaskSchedule repeat,
                                       @NotNull ExecutionType executionType) {
        return buildTask(task).delay(delay).repeat(repeat).executionType(executionType).schedule();
    }

    default @NotNull Task scheduleTask(@NotNull Runnable task, @NotNull TaskSchedule delay, @NotNull TaskSchedule repeat) {
        return scheduleTask(task, delay, repeat, ExecutionType.TICK_START);
    }

    default @NotNull Task scheduleTask(@NotNull Supplier<TaskSchedule> task, @NotNull TaskSchedule delay) {
        return new Task.Builder(this, task).delay(delay).schedule();
    }

    default @NotNull Task scheduleNextTick(@NotNull Runnable task, @NotNull ExecutionType executionType) {
        return buildTask(task).delay(TaskSchedule.nextTick()).executionType(executionType).schedule();
    }

    default @NotNull Task scheduleNextTick(@NotNull Runnable task) {
        return scheduleNextTick(task, ExecutionType.TICK_START);
    }

    default @NotNull Task scheduleEndOfTick(@NotNull Runnable task) {
        return scheduleNextProcess(task, ExecutionType.TICK_END);
    }

    default @NotNull Task scheduleNextProcess(@NotNull Runnable task, @NotNull ExecutionType executionType) {
        return buildTask(task).delay(TaskSchedule.immediate()).executionType(executionType).schedule();
    }

    default @NotNull Task scheduleNextProcess(@NotNull Runnable task) {
        return scheduleNextProcess(task, ExecutionType.TICK_START);
    }

    /**
     * Implementation of {@link Executor}, proxies to {@link #scheduleNextTick(Runnable)}.
     * @param command the task to execute on the next tick
     */
    @Override
    default void execute(@NotNull Runnable command) {
        scheduleNextTick(command);
    }
}
