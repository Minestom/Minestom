package net.minestom.server.event;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an element which can have {@link Event} listeners assigned to it.
 */
@ApiStatus.NonExtendable
public interface EventHandler<T extends Event> {
    @NotNull EventNode<T> eventNode();
}
