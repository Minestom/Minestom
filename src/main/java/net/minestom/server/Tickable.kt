package net.minestom.server

/**
 * Represents an element which is ticked at a regular interval.
 */
interface Tickable {
    /**
     * Ticks this element.
     *
     * @param time the time of the tick in milliseconds
     */
    fun tick(time: Long)
}