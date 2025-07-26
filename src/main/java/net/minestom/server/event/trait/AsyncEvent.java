package net.minestom.server.event.trait;

import net.minestom.server.event.Event;

/**
 * Represents an {@link Event} that is guaranteed to be called within a Virtual Thread.
 */
public interface AsyncEvent extends Event {
}
