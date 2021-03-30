package net.minestom.server.utils;

import net.minestom.server.MinecraftServer;
import net.minestom.server.utils.time.Tick;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

/**
 * Tick related utilities.
 * @deprecated Use {@link Tick}
 */
@Deprecated
public class TickUtils {
    /**
     * Number of ticks per second for the default Java-edition client.
     * @deprecated Use some math
     */
    public static final int CLIENT_TPS = 20;

    /**
     * Length of time per tick for the default Java-edition client.
     * @deprecated Use {@link Tick#getDuration()} on {@link Tick#CLIENT}
     */
    @Deprecated
    public static final int CLIENT_TICK_MS = 50;

    /**
     * Creates a number of ticks from a given duration, based on {@link MinecraftServer#TICK_MS}.
     * @param duration the duration
     * @return the number of ticks
     * @throws IllegalArgumentException if duration is negative
     * @deprecated Use {@link Tick#fromDuration(Duration)} on {@link Tick#SERVER}
     */
    @Deprecated
    public static int fromDuration(@NotNull Duration duration) {
        return TickUtils.fromDuration(duration, MinecraftServer.TICK_MS);
    }

    /**
     * Creates a number of ticks from a given duration.
     * @param duration the duration
     * @param msPerTick the number of milliseconds per tick
     * @return the number of ticks
     * @throws IllegalArgumentException if duration is negative
     * @deprecated Use {@link Tick#fromDuration(Duration)} on {@link Tick#SERVER} or {@link Tick#CLIENT}
     */
    @Deprecated
    public static int fromDuration(@NotNull Duration duration, int msPerTick) {
        Validate.isTrue(!duration.isNegative(), "Duration cannot be negative");

        return (int) (duration.toMillis() / msPerTick);
    }
}
