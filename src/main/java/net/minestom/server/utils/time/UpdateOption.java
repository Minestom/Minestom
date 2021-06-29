package net.minestom.server.utils.time;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @deprecated Replaced by {@link java.time.Duration}
 */
@Deprecated(forRemoval = true)
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

    @Override
    public int hashCode() {
        return Objects.hash(value, timeUnit);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpdateOption updateOption = (UpdateOption) o;
        return Objects.equals(value, updateOption.value) && Objects.equals(timeUnit, updateOption.timeUnit);
    }

    /**
     * Converts this update option to milliseconds
     *
     * @return the converted milliseconds based on the time value and the unit
     */
    public long toMilliseconds() {
        return timeUnit.toMilliseconds(value);
    }
}
