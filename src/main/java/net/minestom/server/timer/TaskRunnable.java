package net.minestom.server.timer;

public abstract class TaskRunnable {

    private int id;
    private int callCount;

    public abstract void run();

    public int getId() {
        return id;
    }

    public int getCallCount() {
        return callCount;
    }

    protected void setId(int id) {
        this.id = id;
    }

    protected void setCallCount(int callCount) {
        this.callCount = callCount;
    }
}
