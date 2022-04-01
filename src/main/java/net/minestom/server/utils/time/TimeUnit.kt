package net.minestom.server.utils.time

import java.time.temporal.TemporalUnit
import java.lang.IllegalArgumentException
import java.time.temporal.Temporal
import net.minestom.server.MinecraftServer
import net.minestom.server.utils.time.Cooldown
import java.time.temporal.ChronoUnit

object TimeUnit {
    val DAY: TemporalUnit = ChronoUnit.DAYS
    val HOUR: TemporalUnit = ChronoUnit.HOURS
    val MINUTE: TemporalUnit = ChronoUnit.MINUTES
    val SECOND: TemporalUnit = ChronoUnit.SECONDS
    val MILLISECOND: TemporalUnit = ChronoUnit.MILLIS
    val SERVER_TICK: TemporalUnit = Tick.Companion.SERVER_TICKS
    val CLIENT_TICK: TemporalUnit = Tick.Companion.CLIENT_TICKS

    @Deprecated("Please use either {@link #SERVER_TICK} or {@link #CLIENT_TICK}")
    val TICK = CLIENT_TICK
}