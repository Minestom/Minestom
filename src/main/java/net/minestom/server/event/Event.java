package net.minestom.server.event;

import net.minestom.server.event.handler.EventHandler;

/**
 * Object which can be listened to by an {@link EventHandler}.
 * <p>
 * Called using {@link EventHandler#callEvent(Class, Event)}.
 */
public class Event {
    public static <T extends EntityEvent> EventListener.Builder<T> entity(Class<T> eventType) {
        return new EventListener.Builder<>(eventType);
    }

    public static <T extends PlayerEvent> EventListener.Builder<T> player(Class<T> eventType) {
        return new EventListener.Builder<>(eventType);
    }
}
