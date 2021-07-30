package net.minestom.server.utils.time;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.temporal.TemporalUnit;

public final class Cooldown {
    private final Duration duration;
    private long lastUpdate;

    public Cooldown(Duration duration) {
        this.duration = duration;
        this.lastUpdate = System.currentTimeMillis();
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
     * @param currentTime  the current time in milliseconds
     * @param lastUpdate   the last update in milliseconds
     * @param temporalUnit the time unit of the cooldown
     * @param cooldown     the value of the cooldown
     * @return true if the cooldown is in progress, false otherwise
     */
    public static boolean hasCooldown(long currentTime, long lastUpdate, @NotNull TemporalUnit temporalUnit, long cooldown) {
        return hasCooldown(currentTime, lastUpdate, Duration.of(cooldown, temporalUnit));
    }

    /**
     * Gets if something is in cooldown based on the current time.
     *
     * @param currentTime the current time in milliseconds
     * @param lastUpdate  the last update in milliseconds
     * @param duration    the cooldown
     * @return true if the cooldown is in progress, false otherwise
     */
    public static boolean hasCooldown(long currentTime, long lastUpdate, @NotNull Duration duration) {
        final long cooldownMs = duration.toMillis();
        return currentTime - lastUpdate < cooldownMs;
    }

    /**
     * Gets if something is in cooldown based on the current time ({@link System#currentTimeMillis()}).
     *
     * @param lastUpdate   the last update in milliseconds
     * @param temporalUnit the time unit of the cooldown
     * @param cooldown     the value of the cooldown
     * @return true if the cooldown is in progress, false otherwise
     */
    public static boolean hasCooldown(long lastUpdate, @NotNull TemporalUnit temporalUnit, int cooldown) {
        return hasCooldown(System.currentTimeMillis(), lastUpdate, temporalUnit, cooldown);
    }
}
