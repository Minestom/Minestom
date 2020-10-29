package net.minestom.server.timer;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minestom.server.utils.time.TimeUnit;

/**
 * A builder which represents a fluent Object to schedule tasks.
 * <p>
 * You can specify a delay with {@link #delay(long, TimeUnit)} or {@link #repeat(long, TimeUnit)}
 * and then schedule the {@link Task} with {@link #schedule()}.
 */
public class TaskBuilder {

    // Manager for the tasks
    private final SchedulerManager schedulerManager;
    // The logic behind every task
    private final Runnable runnable;
    // True if the task planned for the application shutdown
    private final boolean shutdown;
    // Delay value for the task execution
    private long delay;
    // Repeat value for the task execution
    private long repeat;

    /**
     * Creates a task builder.
     * <br>
     * <b>Note:</b> The task builder creates a normal task.
     *
     * @param schedulerManager The manager for the tasks
     * @param runnable         The task to run when scheduled
     */
    public TaskBuilder(SchedulerManager schedulerManager, Runnable runnable) {
        this(schedulerManager, runnable, false);
    }

    /**
     * Creates a task builder.
     *
     * @param schedulerManager The manager for the tasks
     * @param runnable         The task to run when scheduled
     * @param shutdown         Defines whether the task is a shutdown task
     */
    public TaskBuilder(SchedulerManager schedulerManager, Runnable runnable, boolean shutdown) {
        this.schedulerManager = schedulerManager;
        this.runnable = runnable;
        this.shutdown = shutdown;
    }

    /**
     * Specifies that the {@link Task} should delay its execution by the specified amount of time.
     *
     * @param time The time to delay
     * @param unit The unit of time for {@code time}
     * @return this builder, for chaining
     */
    public TaskBuilder delay(long time, TimeUnit unit) {
        this.delay = unit.toMilliseconds(time);
        return this;
    }

    /**
     * Specifies that the {@link Task} should continue to run after waiting for the specified value until it is terminated.
     *
     * @param time The time until the repetition
     * @param unit The {@link TimeUnit} for {@code time}
     * @return this builder, for chaining
     */
    public TaskBuilder repeat(long time, TimeUnit unit) {
        this.repeat = unit.toMilliseconds(time);
        return this;
    }

    /**
     * Clears the delay interval of the {@link Task}.
     *
     * @return this builder, for chaining
     */
    public TaskBuilder clearDelay() {
        this.delay = 0L;
        return this;
    }

    /**
     * Clears the repeat interval of the {@link Task}.
     *
     * @return this builder, for chaining
     */
    public TaskBuilder clearRepeat() {
        this.repeat = 0L;
        return this;
    }

    /**
     * Schedule this {@link Task} for execution.
     *
     * @return the built {@link Task}
     */
    public Task schedule() {
        Task task = new Task(
                this.schedulerManager,
                this.runnable,
                this.shutdown,
                this.delay,
                this.repeat);
        if (this.shutdown) {
            Int2ObjectMap<Task> shutdownTasks = this.schedulerManager.shutdownTasks;
            synchronized (shutdownTasks) {
                shutdownTasks.put(task.getId(), task);
            }
        } else {
            Int2ObjectMap<Task> tasks = this.schedulerManager.tasks;
            synchronized (tasks) {
                tasks.put(task.getId(), task);
            }
            task.schedule();
        }
        return task;
    }
}
