package net.minestom.server.event.handler;

import net.minestom.server.event.CancellableEvent;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventCallback;
import net.minestom.server.utils.validate.Check;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

/**
 * Represents an element which can have {@link Event} listeners assigned to it
 */
public interface EventHandler {

    /**
     * Get a {@link Map} containing all the listeners assigned to a specific {@link Event} type
     *
     * @return a {@link Map} with all the listeners
     */
    Map<Class<? extends Event>, Collection<EventCallback>> getEventCallbacksMap();

    /**
     * Add a new event callback for the specified type {@code eventClass}
     *
     * @param eventClass    the event class
     * @param eventCallback the event callback
     * @param <E>           the event type
     */
    default <E extends Event> void addEventCallback(Class<E> eventClass, EventCallback<E> eventCallback) {
        Check.notNull(eventClass, "Event class cannot be null");
        Check.notNull(eventCallback, "Event callback cannot be null");
        Collection<EventCallback> callbacks = getEventCallbacks(eventClass);
        callbacks.add(eventCallback);
    }

    /**
     * Remove an event callback
     *
     * @param eventClass    the event class
     * @param eventCallback the event callback
     * @param <E>           the event type
     */
    default <E extends Event> void removeEventCallback(Class<E> eventClass, EventCallback<E> eventCallback) {
        Check.notNull(eventClass, "Event class cannot be null");
        Check.notNull(eventCallback, "Event callback cannot be null");
        Collection<EventCallback> callbacks = getEventCallbacks(eventClass);
        callbacks.remove(eventCallback);
    }

    /**
     * Get the event callbacks of a specific event type
     *
     * @param eventClass the event class
     * @param <E>        the event type
     * @return all event callbacks for the specified type {@code eventClass}
     */
    default <E extends Event> Collection<EventCallback> getEventCallbacks(Class<E> eventClass) {
        Check.notNull(eventClass, "Event class cannot be null");
        return getEventCallbacksMap().computeIfAbsent(eventClass, clazz -> new CopyOnWriteArrayList<>());
    }

    /**
     * Get a {@link Stream} containing all the {@link EventCallback}, no matter to which {@link Event} they are linked
     *
     * @return a {@link Stream} containing all the callbacks
     */
    default Stream<EventCallback> getEventCallbacks() {
        return getEventCallbacksMap().values().stream().flatMap(Collection::stream);
    }

    /**
     * Call the specified {@link Event} with all the assigned {@link EventCallback}
     *
     * @param eventClass the event class
     * @param event      the event object
     * @param <E>        the event type
     */
    default <E extends Event> void callEvent(Class<E> eventClass, E event) {
        // TODO global event
        final Collection<EventCallback> eventCallbacks = getEventCallbacks(eventClass);
        for (EventCallback<E> eventCallback : eventCallbacks) {
            eventCallback.run(event);
        }
    }

    /**
     * Call a {@link CancellableEvent} and execute {@code successCallback} if the event is not cancelled
     * <p>
     * Does call {@link #callEvent(Class, Event)} internally
     *
     * @param eventClass      the event class
     * @param event           the event object
     * @param successCallback the callback called when the event is not cancelled
     * @param <E>             the event type
     * @see #callEvent(Class, Event)
     */
    default <E extends CancellableEvent> void callCancellableEvent(Class<E> eventClass, E event, Runnable successCallback) {
        callEvent(eventClass, event);
        if (!event.isCancelled()) {
            successCallback.run();
        }
    }

}
