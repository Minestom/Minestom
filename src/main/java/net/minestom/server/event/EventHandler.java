package net.minestom.server.event;


/**
 * Represents an element which can have {@link Event} listeners assigned to it.
 */
public interface EventHandler<T extends Event> {
    EventNode<T> eventNode();
}
