package fr.themode.minestom.timer;

import fr.themode.minestom.utils.time.UpdateOption;

public class Task {

    private TaskRunnable runnable;
    private UpdateOption updateOption;

    private long lastUpdateTime;

    public Task(TaskRunnable runnable, UpdateOption updateOption) {
        this.runnable = runnable;
        this.updateOption = updateOption;
    }

    protected void refreshLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    protected long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public TaskRunnable getRunnable() {
        return runnable;
    }

    public UpdateOption getUpdateOption() {
        return updateOption;
    }
}
