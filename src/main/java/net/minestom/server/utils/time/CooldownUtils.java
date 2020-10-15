package net.minestom.server.utils.time;

public final class CooldownUtils {

    private CooldownUtils() {

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
    public static boolean hasCooldown(long currentTime, long lastUpdate, TimeUnit timeUnit, int cooldown) {
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
    public static boolean hasCooldown(long currentTime, long lastUpdate, UpdateOption updateOption) {
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
    public static boolean hasCooldown(long lastUpdate, TimeUnit timeUnit, int cooldown) {
        return hasCooldown(System.currentTimeMillis(), lastUpdate, timeUnit, cooldown);
    }
}
