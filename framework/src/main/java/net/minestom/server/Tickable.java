package net.minestom.server;

/**
 * Represents an element which is ticked at a regular interval.
 */
public interface Tickable {

    /**
     * Ticks this element.
     *
     * @param time the time of the tick in milliseconds
     */
    void tick(long time);
}
