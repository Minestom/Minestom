package net.minestom.server.event;

import org.jetbrains.annotations.NotNull;

/**
 * Represents an element which can have {@link Event} listeners assigned to it.
 */
public interface EventHandler<T extends Event> {
    @NotNull EventNode<T> eventNode();
}
