package net.minestom.server.timer;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

record TaskImpl(int id,
                @NotNull Supplier<TaskSchedule> task,
                @NotNull ExecutionType executionType,
                @NotNull SchedulerImpl owner) implements Task {
    @Override
    public void unpark() {
        this.owner.unparkTask(this);
    }

    @Override
    public boolean isParked() {
        return owner.isTaskParked(this);
    }

    @Override
    public void cancel() {
        this.owner.cancelTask(this);
    }

    @Override
    public boolean isAlive() {
        return owner.isTaskAlive(this);
    }
}
