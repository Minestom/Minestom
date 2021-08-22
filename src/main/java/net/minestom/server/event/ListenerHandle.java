package net.minestom.server.event;

import org.jetbrains.annotations.ApiStatus;

/**
 * Represents a key to access an {@link EventNode} listeners.
 * Useful to avoid map lookups.
 *
 * @param <E> the event type
 */
@ApiStatus.NonExtendable
public interface ListenerHandle<E extends Event> {
}
