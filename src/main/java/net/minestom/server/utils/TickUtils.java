package net.minestom.server.utils;

import net.minestom.server.MinecraftServer;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

/**
 * Tick related utilities.
 */
public class TickUtils {

    /**
     * Creates a number of ticks from a given duration, based on {@link MinecraftServer#TICK_MS}.
     * @param duration the duration
     * @return the number of ticks
     * @throws IllegalArgumentException if duration is negative
     */
    public static int fromDuration(@NotNull Duration duration) {
        Validate.isTrue(!duration.isNegative(), "Duration cannot be negative");

        return (int) (duration.toMillis() / MinecraftServer.TICK_MS);
    }
}
