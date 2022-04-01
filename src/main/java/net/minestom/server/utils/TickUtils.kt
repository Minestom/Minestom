package net.minestom.server.utils

import java.lang.StringBuilder
import net.minestom.server.utils.TickUtils
import net.minestom.server.MinecraftServer
import net.minestom.server.utils.UniqueIdUtils
import net.minestom.server.utils.validate.Check
import java.time.Duration

/**
 * Tick related utilities.
 */
object TickUtils {
    /**
     * Number of ticks per second for the default Java-edition client.
     */
    const val CLIENT_TPS = 20

    /**
     * Length of time per tick for the default Java-edition client.
     */
    const val CLIENT_TICK_MS = 50

    /**
     * Creates a number of ticks from a given duration, based on [MinecraftServer.TICK_MS].
     *
     * @param duration the duration
     * @return the number of ticks
     * @throws IllegalArgumentException if duration is negative
     */
    fun fromDuration(duration: Duration): Int {
        return fromDuration(duration, MinecraftServer.TICK_MS)
    }

    /**
     * Creates a number of ticks from a given duration.
     *
     * @param duration  the duration
     * @param msPerTick the number of milliseconds per tick
     * @return the number of ticks
     * @throws IllegalArgumentException if duration is negative
     */
    fun fromDuration(duration: Duration, msPerTick: Int): Int {
        Check.argCondition(duration.isNegative, "Duration cannot be negative")
        return (duration.toMillis() / msPerTick).toInt()
    }
}