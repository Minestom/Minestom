package net.minestom.server.timer;

import net.minestom.server.extras.selfmodification.MinestomRootClassLoader;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.temporal.TemporalUnit;

/**
 * A builder which represents a fluent Object to schedule tasks.
 * <p>
 * You can specify a delay with {@link #delay(long, TemporalUnit)} or {@link #repeat(long, TemporalUnit)}
 * and then schedule the {@link Task} with {@link #schedule()}.
 */
public class TaskBuilder {

    // Manager for the tasks
    private final SchedulerManager schedulerManager;
    // The logic behind every task
    private final Runnable runnable;
    // True if the task planned for the application shutdown
    private final boolean shutdown;
    /**
     * Extension which owns this task, or null if none
     */
    private final String owningExtension;
    // Delay value for the task execution
    private long delay;
    // Repeat value for the task execution
    private long repeat;
    /**
     * If this task is owned by an extension, should it survive the unloading of said extension?
     * May be useful for delay tasks, but it can prevent the extension classes from being unloaded, and preventing a full
     * reload of that extension.
     */
    private boolean isTransient;

    /**
     * Creates a task builder.
     * <br>
     * <b>Note:</b> The task builder creates a normal task.
     *
     * @param schedulerManager The manager for the tasks
     * @param runnable         The task to run when scheduled
     */
    public TaskBuilder(@NotNull SchedulerManager schedulerManager, @NotNull Runnable runnable) {
        this(schedulerManager, runnable, false);
    }

    /**
     * Creates a task builder.
     *
     * @param schedulerManager The manager for the tasks
     * @param runnable         The task to run when scheduled
     * @param shutdown         Defines whether the task is a shutdown task
     */
    public TaskBuilder(@NotNull SchedulerManager schedulerManager, @NotNull Runnable runnable, boolean shutdown) {
        this.schedulerManager = schedulerManager;
        this.runnable = runnable;
        this.shutdown = shutdown;
        this.isTransient = false;
        this.owningExtension = MinestomRootClassLoader.findExtensionObjectOwner(runnable);
    }

    /**
     * Specifies that the {@link Task} should delay its execution by the specified amount of time.
     *
     * @param time The time to delay
     * @param unit The unit of time for {@code time}
     * @return this builder, for chaining
     */
    public @NotNull TaskBuilder delay(long time, @NotNull TemporalUnit unit) {
        return delay(Duration.of(time, unit));
    }

    /**
     * Specifies that the {@link Task} should delay its execution by the specified amount of time.
     *
     * @param duration the Duration for this builder.
     * @return this builder, for chaining
     */
    public @NotNull TaskBuilder delay(@NotNull Duration duration) {
        this.delay = duration.toMillis();
        return this;
    }

    /**
     * Specifies that the {@link Task} should continue to run after waiting for the specified value until it is terminated.
     *
     * @param time The time until the repetition
     * @param unit The {@link TemporalUnit} for {@code time}
     * @return this builder, for chaining
     */
    public @NotNull TaskBuilder repeat(long time, @NotNull TemporalUnit unit) {
        return repeat(Duration.of(time, unit));
    }

    /**
     * Specifies that the {@link Task} should continue to run after waiting for the specified value until it is terminated.
     *
     * @param duration the Duration for this builder.
     * @return this builder, for chaining
     */
    public @NotNull TaskBuilder repeat(@NotNull Duration duration) {
        this.repeat = duration.toMillis();
        return this;
    }

    /**
     * Clears the delay interval of the {@link Task}.
     *
     * @return this builder, for chaining
     */
    public @NotNull TaskBuilder clearDelay() {
        this.delay = 0L;
        return this;
    }

    /**
     * Clears the repeat interval of the {@link Task}.
     *
     * @return this builder, for chaining
     */
    public @NotNull TaskBuilder clearRepeat() {
        this.repeat = 0L;
        return this;
    }

    /**
     * If this task is owned by an extension, should it survive the unloading of said extension?
     * May be useful for delay tasks, but it can prevent the extension classes from being unloaded, and preventing a full
     * reload of that extension.
     */
    public TaskBuilder makeTransient() {
        isTransient = true;
        return this;
    }

    /**
     * Builds a {@link Task}.
     *
     * @return the built {@link Task}
     */
    @NotNull
    public Task build() {
        return new Task(
                this.schedulerManager,
                this.runnable,
                this.shutdown,
                this.delay,
                this.repeat,
                this.isTransient,
                this.owningExtension);
    }

    /**
     * Schedules this {@link Task} for execution.
     *
     * @return the scheduled {@link Task}
     */
    @NotNull
    public Task schedule() {
        Task task = build();
        task.schedule();
        return task;
    }
}
