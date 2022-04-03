package net.minestom.server.utils.time

import java.time.temporal.TemporalUnit
import java.lang.IllegalArgumentException
import java.time.temporal.Temporal
import net.minestom.server.MinecraftServer
import net.minestom.server.utils.time.Cooldown
import java.time.Duration
import java.time.temporal.ChronoUnit

class Cooldown(val duration: Duration) {
    private var lastUpdate: Long

    init {
        lastUpdate = System.currentTimeMillis()
    }

    fun refreshLastUpdate(lastUpdate: Long) {
        this.lastUpdate = lastUpdate
    }

    fun isReady(time: Long): Boolean {
        return !hasCooldown(time, lastUpdate, duration)
    }

    companion object {
        /**
         * Gets if something is in cooldown based on the current time.
         *
         * @param currentTime  the current time in milliseconds
         * @param lastUpdate   the last update in milliseconds
         * @param temporalUnit the time unit of the cooldown
         * @param cooldown     the value of the cooldown
         * @return true if the cooldown is in progress, false otherwise
         */
        fun hasCooldown(currentTime: Long, lastUpdate: Long, temporalUnit: TemporalUnit, cooldown: Long): Boolean {
            return hasCooldown(currentTime, lastUpdate, Duration.of(cooldown, temporalUnit))
        }

        /**
         * Gets if something is in cooldown based on the current time.
         *
         * @param currentTime the current time in milliseconds
         * @param lastUpdate  the last update in milliseconds
         * @param duration    the cooldown
         * @return true if the cooldown is in progress, false otherwise
         */
        fun hasCooldown(currentTime: Long, lastUpdate: Long, duration: Duration): Boolean {
            val cooldownMs = duration.toMillis()
            return currentTime - lastUpdate < cooldownMs
        }

        /**
         * Gets if something is in cooldown based on the current time ([System.currentTimeMillis]).
         *
         * @param lastUpdate   the last update in milliseconds
         * @param temporalUnit the time unit of the cooldown
         * @param cooldown     the value of the cooldown
         * @return true if the cooldown is in progress, false otherwise
         */
        fun hasCooldown(lastUpdate: Long, temporalUnit: TemporalUnit, cooldown: Int): Boolean {
            return hasCooldown(System.currentTimeMillis(), lastUpdate, temporalUnit, cooldown.toLong())
        }
    }
}