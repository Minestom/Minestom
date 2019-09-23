package fr.themode.minestom.timer;

public abstract class TaskRunnable {

    private int id;

    public abstract void run();

    public int getId() {
        return id;
    }

    protected void setId(int id) {
        this.id = id;
    }
}
