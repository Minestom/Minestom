package net.minestom.server.utils.time;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.Objects;

/**
 * @deprecated Replaced by {@link java.time.Duration}
 */
@Deprecated(forRemoval = true)
public class UpdateOption {

    private final long value;
    private final TemporalUnit temporalUnit;

    public UpdateOption(long value, @NotNull TemporalUnit temporalUnit) {
        this.value = value;
        this.temporalUnit = temporalUnit;
    }

    public long getValue() {
        return value;
    }

    @NotNull
    public TemporalUnit getTemporalUnit() {
        return temporalUnit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, temporalUnit);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpdateOption updateOption = (UpdateOption) o;
        return Objects.equals(value, updateOption.value) && Objects.equals(temporalUnit, updateOption.temporalUnit);
    }

    /**
     * Converts this update option to milliseconds
     *
     * @return the converted milliseconds based on the time value and the unit
     */
    public long toMilliseconds() {
        return toDuration().toMillis();
    }

    public Duration toDuration() {
        return Duration.of(value, temporalUnit);
    }
}
