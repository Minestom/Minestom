package net.minestom.server.event.handler;

import net.minestom.server.event.Event;
import net.minestom.server.event.EventCallback;
import net.minestom.server.event.EventNode;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an element which can have {@link Event} listeners assigned to it.
 */
public interface EventHandler<T extends Event> {

    @NotNull EventNode<T> getEventNode();

    default <V extends T> boolean addEventCallback(@NotNull Class<V> eventClass, @NotNull EventCallback<V> eventCallback) {
        var node = getEventNode();
        node.addListener(eventClass, eventCallback::run);
        return true;
    }
}
