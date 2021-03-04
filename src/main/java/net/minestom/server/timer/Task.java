package net.minestom.server.timer;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * An Object that represents a task that is scheduled for execution on the application.
 * <p>
 * Tasks are built in {@link SchedulerManager} and scheduled by a {@link TaskBuilder}.
 */
public class Task implements Runnable {

    // Manages all tasks
    private final SchedulerManager schedulerManager;
    // The task logic
    private final Runnable runnable;
    // Task identifier
    private final int id;
    // True if the task planned for the application shutdown
    private final boolean shutdown;
    // Delay value for the task execution
    private final long delay;
    // Repeat value for the task execution
    private final long repeat;

    /** Extension which owns this task, or null if none */
    private final String owningExtension;
    /**
     * If this task is owned by an extension, should it survive the unloading of said extension?
     *  May be useful for delay tasks, but it can prevent the extension classes from being unloaded, and preventing a full
     *  reload of that extension.
     */
    private final boolean isTransient;

    // Task completion/execution
    private ScheduledFuture<?> future;
    // The thread of the task
    private volatile Thread currentThreadTask;

    /**
     * Creates a task.
     *
     * @param schedulerManager The manager for the task
     * @param runnable         The task to run when scheduled
     * @param shutdown         Defines whether the task is a shutdown task
     * @param delay            The time to delay
     * @param repeat           The time until the repetition
     */
    public Task(@NotNull SchedulerManager schedulerManager, @NotNull Runnable runnable, boolean shutdown, long delay, long repeat, boolean isTransient, @Nullable String owningExtension) {
        this.schedulerManager = schedulerManager;
        this.runnable = runnable;
        this.shutdown = shutdown;
        this.id = shutdown ? this.schedulerManager.getShutdownCounterIdentifier() : this.schedulerManager.getCounterIdentifier();
        this.delay = delay;
        this.repeat = repeat;
        this.isTransient = isTransient;
        this.owningExtension = owningExtension;
    }

    /**
     * Executes the task.
     */
    @Override
    public void run() {
        this.schedulerManager.getBatchesPool().execute(() -> {
            this.currentThreadTask = Thread.currentThread();
            try {
                this.runnable.run();
            } catch (Exception e) {
                System.err.printf(
                        "An exception in %s task %s is occurred! (%s)%n",
                        this.shutdown ? "shutdown" : "",
                        this.id,
                        e.getMessage()
                );
                MinecraftServer.getExceptionManager().handleException(e);
            } finally {
                if (this.repeat == 0) this.finish();
                this.currentThreadTask = null;
            }
        });
    }

    /**
     * Executes the internal runnable.
     * <p>
     * Should probably use {@link #schedule()} instead.
     */
    public void runRunnable() {
        this.runnable.run();
    }

    /**
     * Sets up the task for correct execution.
     */
    public void schedule() {
        if(owningExtension != null) {
            this.schedulerManager.onScheduleFromExtension(owningExtension, this);
        }
        this.future = this.repeat == 0L ?
                this.schedulerManager.getTimerExecutionService().schedule(this, this.delay, TimeUnit.MILLISECONDS) :
                this.schedulerManager.getTimerExecutionService().scheduleAtFixedRate(this, this.delay, this.repeat, TimeUnit.MILLISECONDS);
    }

    /**
     * Gets the current status of the task.
     *
     * @return the current stats of the task
     */
    @NotNull
    public TaskStatus getStatus() {
        if (this.future == null) return TaskStatus.SCHEDULED;
        if (this.future.isCancelled()) return TaskStatus.CANCELLED;
        if (this.future.isDone()) return TaskStatus.FINISHED;
        return TaskStatus.SCHEDULED;
    }

    /**
     * Cancels this task. If the task is already running, the thread in which it is running is interrupted.
     * If the task is not currently running, Minestom will safely terminate it.
     */
    public void cancel() {
        if (this.future != null) {
            this.future.cancel(false);

            Thread current = this.currentThreadTask;
            if (current != null) current.interrupt();

            this.finish();
        }
    }

    /**
     * Gets the id of this task.
     *
     * @return the task id
     */
    public int getId() {
        return id;
    }

    /**
     * Removes the task from the {@link SchedulerManager} map.
     */
    private void finish() {
        Int2ObjectMap<Task> taskMap = shutdown ?
                this.schedulerManager.shutdownTasks :
                this.schedulerManager.tasks;

        synchronized (taskMap) {
            taskMap.remove(getId());
        }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Task task = (Task) object;
        return id == task.id;
    }

    /**
     * If this task is owned by an extension, should it survive the unloading of said extension?
     *  May be useful for delay tasks, but it can prevent the extension classes from being unloaded, and preventing a full
     *  reload of that extension.
     */
    public boolean isTransient() {
        return isTransient;
    }

    /**
     * Extension which owns this task, or null if none
     */
    public String getOwningExtension() {
        return owningExtension;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
