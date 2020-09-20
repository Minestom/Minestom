package net.minestom.server.event.handler;

import net.minestom.server.event.CancellableEvent;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventCallback;

import java.util.List;
import java.util.stream.Stream;

public interface EventHandler {

    /**
     * Add a new event callback for the specified type {@code eventClass}
     *
     * @param eventClass    the event class
     * @param eventCallback the event callback
     * @param <E>           the event type
     */
    <E extends Event> void addEventCallback(Class<E> eventClass, EventCallback<E> eventCallback);

    /**
     * Remove an event callback
     *
     * @param eventClass    the event class
     * @param eventCallback the event callback
     * @param <E>           the event type
     */
    <E extends Event> void removeEventCallback(Class<E> eventClass, EventCallback<E> eventCallback);

    /**
     * Get the event callbacks of a specific event type
     *
     * @param eventClass the event class
     * @param <E>        the event type
     * @return all event callbacks for the specified type {@code eventClass}
     */
    <E extends Event> List<EventCallback> getEventCallbacks(Class<E> eventClass);


    /**
     * Get a {@link Stream} containing all the added callbacks, no matter to which event they are linked
     *
     * @return a {@link Stream} containing all the added callbacks
     */
    Stream<EventCallback> getEventCallbacks();

    /**
     * Call the specified event type using the Event object parameter
     *
     * @param eventClass the event class
     * @param event      the event object
     * @param <E>        the event type
     */
    default <E extends Event> void callEvent(Class<E> eventClass, E event) {
        final List<EventCallback> eventCallbacks = getEventCallbacks(eventClass);
        for (EventCallback<E> eventCallback : eventCallbacks) {
            eventCallback.run(event);
        }
    }

    /**
     * Same as {@link #callEvent(Class, Event)} but add a Runnable which is called if the event is not cancelled
     *
     * @param eventClass the event class
     * @param event      the event object
     * @param runnable   the callback called when the event is not cancelled
     * @param <E>        the event type
     */
    default <E extends CancellableEvent> void callCancellableEvent(Class<E> eventClass, E event, Runnable runnable) {
        callEvent(eventClass, event);
        if (!event.isCancelled()) {
            runnable.run();
        }
    }

}
