package net.minestom.server.utils.time;

import org.jetbrains.annotations.NotNull;

public class UpdateOption {

    private final long value;
    private final TimeUnit timeUnit;

    public UpdateOption(long value, @NotNull TimeUnit timeUnit) {
        this.value = value;
        this.timeUnit = timeUnit;
    }

    public long getValue() {
        return value;
    }

    @NotNull
    public TimeUnit getTimeUnit() {
        return timeUnit;
    }
}
