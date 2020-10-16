package net.minestom.server.utils.time;

import net.minestom.server.MinecraftServer;

public enum TimeUnit {

    TICK, DAY, HOUR, MINUTE, SECOND, MILLISECOND;

    /**
     * Converts a value and its unit to milliseconds.
     *
     * @param value the time value
     * @return the converted milliseconds based on the time value and the unit
     */
    public long toMilliseconds(long value) {
        switch (this) {
            case TICK:
                return MinecraftServer.TICK_MS * value;
            case DAY:
                return value * 86_400_000;
            case HOUR:
                return value * 3_600_000;
            case MINUTE:
                return value * 60_000;
            case SECOND:
                return value * 1000;
            case MILLISECOND:
                return value;
            default:
                return -1; // Unexpected
        }
    }

}
