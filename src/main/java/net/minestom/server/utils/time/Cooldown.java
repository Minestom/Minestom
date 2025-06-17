package net.minestom.server.utils.time;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

public final class Cooldown {
    private final Duration duration;
    private final TemporalUnit temporalUnit;
    // if this cooldown object as a lastUpdate set
    private boolean hasLastUpdate;
    private long lastUpdate;

    /**
     * Creates a cooldown with a measurement unit of {@link ChronoUnit#MILLIS}
     */
    public Cooldown(@NotNull Duration duration) {
        this(duration, ChronoUnit.MILLIS);
    }

    /**
     * Creates a cooldown with a given unit of measurement.
     * <p>
     * All calls to {@link #refreshLastUpdate(long)} and {@link #isReady(long)} must pass values in the given unit.
     *
     * @param duration     the duration of the cooldown
     * @param temporalUnit the unit of measurement
     */
    public Cooldown(@NotNull Duration duration, @NotNull TemporalUnit temporalUnit) {
        this.duration = duration;
        this.temporalUnit = temporalUnit;
        this.hasLastUpdate = false;
    }

    /**
     * @return the unit of measurement
     */
    public @NotNull TemporalUnit getTemporalUnit() {
        return temporalUnit;
    }

    public Duration getDuration() {
        return this.duration;
    }

    /**
     * @param lastUpdate the time of the last update, in nanos
     */
    public void refreshLastUpdate(long lastUpdate) {
        this.hasLastUpdate = true;
        this.lastUpdate = lastUpdate;
    }

    /**
     * Checks if the cooldown is ready again
     *
     * @param time the time, in nanos
     */
    public boolean isReady(long time) {
        if (!hasLastUpdate) return true;
        return !hasCooldown(temporalUnit, time, lastUpdate, duration);
    }

    /**
     * Gets if something is in cooldown based on a {@code currentTime}.
     *
     * @param currentTime  the current time in milliseconds
     * @param lastUpdate   the last update in milliseconds
     * @param cooldownUnit the time unit of the cooldown
     * @param cooldown     the value of the cooldown
     * @return true if the cooldown is in progress, false otherwise
     */
    public static boolean hasCooldown(long currentTime, long lastUpdate, @NotNull TemporalUnit cooldownUnit, long cooldown) {
        return hasCooldown(currentTime, lastUpdate, Duration.of(cooldown, cooldownUnit));
    }

    /**
     * Gets if something is in cooldown based on a {@code currentTime}.
     *
     * @param currentTime the current time in milliseconds
     * @param lastUpdate  the last update in milliseconds
     * @param duration    the cooldown
     * @return true if the cooldown is in progress, false otherwise
     */
    public static boolean hasCooldown(long currentTime, long lastUpdate, @NotNull Duration duration) {
        return hasCooldown(ChronoUnit.MILLIS, currentTime, lastUpdate, duration);
    }

    /**
     * Gets if something is in cooldown based on a {@code currentTime}.
     *
     * @param temporalUnit the {@link TemporalUnit} of {@code currentTime} and {@code lastUpdate}
     * @param currentTime  the current time in milliseconds
     * @param lastUpdate   the last update in milliseconds
     * @param cooldownUnit the time unit of the cooldown
     * @param cooldown     the value of the cooldown
     * @return true if the cooldown is in progress, false otherwise
     */
    public static boolean hasCooldown(@NotNull TemporalUnit temporalUnit, long currentTime, long lastUpdate, @NotNull TemporalUnit cooldownUnit, long cooldown) {
        return hasCooldown(temporalUnit, currentTime, lastUpdate, Duration.of(cooldown, cooldownUnit));
    }

    /**
     * Gets if something is in cooldown based on a {@code currentTime}.
     *
     * @param temporalUnit the {@link TemporalUnit} of {@code currentTime} and {@code lastUpdate}
     * @param currentTime  the current time in the given {@code temporalUnit}
     * @param lastUpdate   the last update in the given {@code temporalUnit}
     * @param duration     the cooldown
     * @return true if the cooldown is in progress, false otherwise
     */
    public static boolean hasCooldown(@NotNull TemporalUnit temporalUnit, long currentTime, long lastUpdate, @NotNull Duration duration) {
        return Duration.of(currentTime - lastUpdate, temporalUnit).compareTo(duration) < 0;
    }

    /**
     * Gets if something is in cooldown based on the current time ({@link System#nanoTime()}).
     *
     * @param lastUpdate   the last update in {@link System#nanoTime()}
     * @param temporalUnit the time unit of the cooldown
     * @param cooldown     the value of the cooldown
     * @return true if the cooldown is in progress, false otherwise
     */
    public static boolean hasCooldown(long lastUpdate, @NotNull TemporalUnit temporalUnit, int cooldown) {
        return hasCooldown(ChronoUnit.NANOS, System.nanoTime(), lastUpdate, temporalUnit, cooldown);
    }
}
