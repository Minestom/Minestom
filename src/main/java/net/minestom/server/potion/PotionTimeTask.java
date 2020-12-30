package net.minestom.server.potion;

import net.minestom.server.timer.SchedulerManager;
import net.minestom.server.timer.Task;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;

public class PotionTimeTask {

    public Long remainingTime;
    public Task task;
    public PotionTask potionTask;

    /**
     * Creates a task.
     *
     * @param schedulerManager The manager for the task
     * @param delay            The time to delay
     */
    public PotionTimeTask(@NotNull SchedulerManager schedulerManager, long delay) {
        Runnable runTask = () -> {
            potionTask.removeEffect();
        };
        task = schedulerManager.buildTask(runTask).delay(delay, TimeUnit.MILLISECOND).schedule();
        this.remainingTime = delay;
    }
}
