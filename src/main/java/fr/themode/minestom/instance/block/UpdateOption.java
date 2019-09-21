package fr.themode.minestom.instance.block;

import fr.themode.minestom.utils.time.TimeUnit;

public class UpdateOption {

    private int value;
    private TimeUnit timeUnit;

    public UpdateOption(int value, TimeUnit timeUnit) {
        this.value = value;
        this.timeUnit = timeUnit;
    }

    public UpdateOption() {
        this(0, null);
    }

    public int getValue() {
        return value;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }
}
