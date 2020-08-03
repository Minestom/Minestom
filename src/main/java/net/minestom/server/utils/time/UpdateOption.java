package net.minestom.server.utils.time;

public class UpdateOption {

    private final int value;
    private final TimeUnit timeUnit;

    public UpdateOption(int value, TimeUnit timeUnit) {
        this.value = value;
        this.timeUnit = timeUnit;
    }

    public int getValue() {
        return value;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }
}
