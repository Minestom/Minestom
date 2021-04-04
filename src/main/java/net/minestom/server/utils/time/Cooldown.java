package net.minestom.server.utils.time;

import org.jetbrains.annotations.NotNull;

public final class Cooldown {

    private UpdateOption updateOption;
    private long lastUpdate;

    public Cooldown(@NotNull UpdateOption updateOption) {
        this.updateOption = updateOption;
        this.lastUpdate = System.currentTimeMillis();
    }

    public UpdateOption getUpdateOption() {
        return this.updateOption;
    }

    public void refreshLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public boolean isReady(long time) {
        return !hasCooldown(time, lastUpdate, updateOption.getTimeUnit(), updateOption.getValue());
    }

    /**
     * Gets if something is in cooldown based on the current time.
     *
     * @param currentTime the current time in milliseconds
     * @param lastUpdate  the last update in milliseconds
     * @param timeUnit    the time unit of the cooldown
     * @param cooldown    the value of the cooldown
     * @return true if the cooldown is in progress, false otherwise
     */
    public static boolean hasCooldown(long currentTime, long lastUpdate, @NotNull TimeUnit timeUnit, long cooldown) {
        final long cooldownMs = timeUnit.toMilliseconds(cooldown);
        return currentTime - lastUpdate < cooldownMs;
    }

    /**
     * Gets if something is in cooldown based on the current time.
     *
     * @param currentTime  the current time in milliseconds
     * @param lastUpdate   the last update in milliseconds
     * @param updateOption the cooldown
     * @return true if the cooldown is in progress, false otherwise
     */
    public static boolean hasCooldown(long currentTime, long lastUpdate, @NotNull UpdateOption updateOption) {
        return hasCooldown(currentTime, lastUpdate, updateOption.getTimeUnit(), updateOption.getValue());
    }

    /**
     * Gets if something is in cooldown based on the current time ({@link System#currentTimeMillis()}).
     *
     * @param lastUpdate the last update in milliseconds
     * @param timeUnit   the time unit of the cooldown
     * @param cooldown   the value of the cooldown
     * @return true if the cooldown is in progress, false otherwise
     */
    public static boolean hasCooldown(long lastUpdate, @NotNull TimeUnit timeUnit, int cooldown) {
        return hasCooldown(System.currentTimeMillis(), lastUpdate, timeUnit, cooldown);
    }
}
