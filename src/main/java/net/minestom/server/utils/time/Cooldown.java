package net.minestom.server.utils.time;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.temporal.TemporalUnit;

@SuppressWarnings("removal")
public final class Cooldown {

    private final Duration duration;
    private long lastUpdate;

    /**
     * @deprecated Replaced by {@link #Cooldown(Duration)}
     */
    @Deprecated(forRemoval = true)
    public Cooldown(@NotNull UpdateOption updateOption) {
        this.duration = Duration.ofMillis(updateOption.toMilliseconds());
        this.lastUpdate = System.currentTimeMillis();
    }

    public Cooldown(Duration duration) {
        this.duration = duration;
    }

    public Duration getDuration() {
        return this.duration;
    }

    public void refreshLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public boolean isReady(long time) {
        return !hasCooldown(time, lastUpdate, duration);
    }

    /**
     * Gets if something is in cooldown based on the current time.
     *
     * @param currentTime the current time in milliseconds
     * @param lastUpdate  the last update in milliseconds
     * @param timeUnit    the time unit of the cooldown
     * @param cooldown    the value of the cooldown
     * @return true if the cooldown is in progress, false otherwise
     *
     * @deprecated Replaced by {@link #hasCooldown(long, long, TemporalUnit, long)}
     */
    @Deprecated(forRemoval = true)
    public static boolean hasCooldown(long currentTime, long lastUpdate, @NotNull TimeUnit timeUnit, long cooldown) {
        final long cooldownMs = timeUnit.toMilliseconds(cooldown);
        return currentTime - lastUpdate < cooldownMs;
    }

    /**
     * Gets if something is in cooldown based on the current time.
     *
     * @param currentTime the current time in milliseconds
     * @param lastUpdate  the last update in milliseconds
     * @param temporalUnit    the time unit of the cooldown
     * @param cooldown    the value of the cooldown
     * @return true if the cooldown is in progress, false otherwise
     */
    public static boolean hasCooldown(long currentTime, long lastUpdate, @NotNull TemporalUnit temporalUnit, long cooldown) {
        return hasCooldown(currentTime, lastUpdate, Duration.of(cooldown, temporalUnit));
    }

    /**
     * Gets if something is in cooldown based on the current time.
     *
     * @param currentTime  the current time in milliseconds
     * @param lastUpdate   the last update in milliseconds
     * @param updateOption the cooldown
     * @return true if the cooldown is in progress, false otherwise
     *
     * @deprecated Replaced by {@link #hasCooldown(long, long, Duration)}
     */
    @Deprecated(forRemoval = true)
    public static boolean hasCooldown(long currentTime, long lastUpdate, @NotNull UpdateOption updateOption) {
        return hasCooldown(currentTime, lastUpdate, updateOption.getTimeUnit(), updateOption.getValue());
    }

    /**
     * Gets if something is in cooldown based on the current time.
     *
     * @param currentTime  the current time in milliseconds
     * @param lastUpdate   the last update in milliseconds
     * @param duration the cooldown
     * @return true if the cooldown is in progress, false otherwise
     */
    public static boolean hasCooldown(long currentTime, long lastUpdate, @NotNull Duration duration) {
        final long cooldownMs = duration.toMillis();
        return currentTime - lastUpdate < cooldownMs;
    }

    /**
     * Gets if something is in cooldown based on the current time ({@link System#currentTimeMillis()}).
     *
     * @param lastUpdate the last update in milliseconds
     * @param timeUnit   the time unit of the cooldown
     * @param cooldown   the value of the cooldown
     * @return true if the cooldown is in progress, false otherwise
     *
     * @deprecated Replaced by {@link #hasCooldown(long, TemporalUnit, int)}
     */
    @Deprecated(forRemoval = true)
    public static boolean hasCooldown(long lastUpdate, @NotNull TimeUnit timeUnit, int cooldown) {
        return hasCooldown(System.currentTimeMillis(), lastUpdate, timeUnit, cooldown);
    }

    /**
     * Gets if something is in cooldown based on the current time ({@link System#currentTimeMillis()}).
     *
     * @param lastUpdate the last update in milliseconds
     * @param temporalUnit   the time unit of the cooldown
     * @param cooldown   the value of the cooldown
     * @return true if the cooldown is in progress, false otherwise
     */
    public static boolean hasCooldown(long lastUpdate, @NotNull TemporalUnit temporalUnit, int cooldown) {
        return hasCooldown(System.currentTimeMillis(), lastUpdate, temporalUnit, cooldown);
    }
}
