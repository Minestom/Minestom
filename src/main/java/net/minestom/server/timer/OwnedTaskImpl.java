package net.minestom.server.timer;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

record OwnedTaskImpl(int id,
                     @NotNull Supplier<TaskSchedule> task,
                     @NotNull ExecutionType executionType,
                     @NotNull SchedulerImpl owner) implements OwnedTask {

    @Override
    public void unpark() {
        this.owner.unparkTask(this);
    }

    @Override
    public void stop() {
        this.owner.stopTask(this);
    }

    @Override
    public boolean isAlive() {
        return owner.isTaskAlive(this);
    }
}
