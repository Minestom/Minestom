package net.minestom.server.event;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a key to access an {@link EventNode} listeners.
 * Useful to avoid map lookups.
 *
 * @param <E> the event type
 */
@ApiStatus.Experimental
@ApiStatus.NonExtendable
public interface ListenerHandle<E extends Event> {
    void call(@NotNull E event);

    /**
     * Gets if any listener has been registered for the given handle.
     * May trigger an update if the cached data is not correct.
     * <p>
     * Useful if you are able to avoid expensive computation in the case where
     * the event is unused. Be aware that {@link #call(Event)}
     * has similar optimization built-in.
     *
     * @return true if the event has 1 or more listeners
     */
    boolean hasListener();
}
