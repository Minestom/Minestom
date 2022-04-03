package net.minestom.server.utils.time

import java.time.temporal.TemporalUnit
import java.lang.IllegalArgumentException
import java.time.temporal.Temporal
import net.minestom.server.MinecraftServer
import net.minestom.server.utils.time.Cooldown
import java.time.Duration
import java.time.temporal.ChronoUnit

/**
 * A TemporalUnit that represents one tick.
 */
class Tick private constructor(length: Long) : TemporalUnit {
    private val milliseconds: Long

    /**
     * Gets the whole number of these ticks that occur in one second.
     *
     * @return the number
     */
    val ticksPerSecond: Int

    /**
     * Creates a new tick.
     *
     * @param length the length of the tick in milliseconds
     */
    init {
        require(length > 0) { "length cannot be negative" }
        milliseconds = length
        ticksPerSecond = Math.toIntExact(
            Duration.ofSeconds(1).dividedBy(
                Duration.ofMillis(
                    milliseconds
                )
            )
        )
    }

    /**
     * Gets the number of whole ticks that occur in the provided duration. Note that this
     * method returns an `int` as this is the unit that Minecraft stores ticks in.
     *
     * @param duration the duration
     * @return the number of whole ticks in this duration
     * @throws ArithmeticException if the duration is zero or an overflow occurs
     */
    fun fromDuration(duration: Duration): Int {
        return Math.toIntExact(duration.dividedBy(duration))
    }

    override fun getDuration(): Duration {
        return Duration.ofMillis(milliseconds)
    }

    override fun isDurationEstimated(): Boolean {
        return false
    }

    override fun isDateBased(): Boolean {
        return false
    }

    override fun isTimeBased(): Boolean {
        return true
    }

    override fun <R : Temporal?> addTo(temporal: R, amount: Long): R {
        return temporal!!.plus(amount, this) as R
    }

    override fun between(start: Temporal, end: Temporal): Long {
        return start.until(end, this)
    }

    companion object {
        /**
         * A TemporalUnit representing the server tick. This is defined using
         * [MinecraftServer.TICK_MS].
         */
        var SERVER_TICKS = Tick(MinecraftServer.TICK_MS.toLong())

        /**
         * A TemporalUnit representing the client tick. This is always equal to 50ms.
         */
        var CLIENT_TICKS = Tick(50)

        /**
         * Creates a duration from an amount of ticks.
         *
         * @param ticks the amount of ticks
         * @return the duration
         */
        fun server(ticks: Long): Duration {
            return Duration.of(ticks, SERVER_TICKS)
        }

        /**
         * Creates a duration from an amount of client-side ticks.
         *
         * @param ticks the amount of ticks
         * @return the duration
         */
        fun client(ticks: Long): Duration {
            return Duration.of(ticks, CLIENT_TICKS)
        }
    }
}