package net.minestom.server.event.handler;

import net.minestom.server.event.CancellableEvent;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventCallback;

import java.util.List;

public interface EventHandler {

    <E extends Event> void addEventCallback(Class<E> eventClass, EventCallback<E> eventCallback);

    <E extends Event> List<EventCallback> getEventCallbacks(Class<E> eventClass);

    default <E extends Event> void callEvent(Class<E> eventClass, E event) {
        List<EventCallback> eventCallbacks = getEventCallbacks(eventClass);
        for (EventCallback<E> eventCallback : eventCallbacks) {
            eventCallback.run(event);
        }
    }

    default <E extends CancellableEvent> void callCancellableEvent(Class<E> eventClass, E event, Runnable runnable) {
        callEvent(eventClass, event);
        if (!event.isCancelled()) {
            runnable.run();
        }
    }

}
