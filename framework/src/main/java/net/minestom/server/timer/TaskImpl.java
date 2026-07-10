package net.minestom.server.timer;

import it.unimi.dsi.fastutil.HashCommon;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.function.Supplier;

final class TaskImpl implements Task {
    private static final VarHandle PARKED;

    static {
        try {
            PARKED = MethodHandles.lookup().findVarHandle(TaskImpl.class, "parked", boolean.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private final int id;
    private final Supplier<TaskSchedule> task;
    private final ExecutionType executionType;
    private final SchedulerImpl owner;

    volatile boolean alive;
    volatile boolean parked;

    TaskImpl(int id,
             Supplier<TaskSchedule> task,
             ExecutionType executionType,
             SchedulerImpl owner) {
        this.id = id;
        this.task = task;
        this.executionType = executionType;
        this.owner = owner;
        this.alive = true;
    }

    @Override
    public void unpark() {
        this.owner.unparkTask(this);
    }

    boolean tryUnpark() {
        return PARKED.compareAndSet(this, true, false);
    }

    @Override
    public boolean isParked() {
        return parked;
    }

    @Override
    public void cancel() {
        this.alive = false;
    }

    @Override
    public boolean isAlive() {
        return alive;
    }

    public int id() {
        return id;
    }

    public Supplier<TaskSchedule> task() {
        return task;
    }

    public ExecutionType executionType() {
        return executionType;
    }

    public SchedulerImpl owner() {
        return owner;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TaskImpl) obj;
        return this.id == that.id;
    }

    @Override
    public int hashCode() {
        return HashCommon.murmurHash3(id);
    }

    @Override
    public String toString() {
        return "TaskImpl[" +
                "id=" + id + ", " +
                "task=" + task + ", " +
                "executionType=" + executionType + ", " +
                "owner=" + owner + ']';
    }

}
