package net.minestom.server.timer;

import net.minestom.server.utils.time.UpdateOption;

public class Task {

    private int id;

    private TaskRunnable runnable;
    private UpdateOption updateOption;

    private int maxCallCount;

    private long lastUpdateTime;

    public Task(TaskRunnable runnable, UpdateOption updateOption, int maxCallCount) {
        this.id = runnable.getId();

        this.runnable = runnable;
        this.updateOption = updateOption;
        this.maxCallCount = maxCallCount;
    }

    protected void refreshLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    protected long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public int getId() {
        return id;
    }

    public TaskRunnable getRunnable() {
        return runnable;
    }

    public UpdateOption getUpdateOption() {
        return updateOption;
    }

    public int getMaxCallCount() {
        return maxCallCount;
    }
}
