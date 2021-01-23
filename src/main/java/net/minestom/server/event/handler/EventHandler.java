package net.minestom.server.event.handler;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventCallback;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Stream;

/**
 * Represents an element which can have {@link Event} listeners assigned to it.
 */
public interface EventHandler {

    /**
     * Gets a {@link Map} containing all the listeners assigned to a specific {@link Event} type.
     *
     * @return a {@link Map} with all the listeners
     */
    @NotNull
    Map<Class<? extends Event>, Collection<EventCallback>> getEventCallbacksMap();

    /**
     * Adds a new event callback for the specified type {@code eventClass}.
     *
     * @param eventClass    the event class
     * @param eventCallback the event callback
     * @param <E>           the event type
     * @return true if the callback collection changed as a result of the call
     */
    default <E extends Event> boolean addEventCallback(@NotNull Class<E> eventClass, @NotNull EventCallback<E> eventCallback) {
        Collection<EventCallback> callbacks = getEventCallbacks(eventClass);
        return callbacks.add(eventCallback);
    }

    /**
     * Removes an event callback.
     *
     * @param eventClass    the event class
     * @param eventCallback the event callback
     * @param <E>           the event type
     * @return true if the callback was removed as a result of this call
     */
    default <E extends Event> boolean removeEventCallback(@NotNull Class<E> eventClass, @NotNull EventCallback<E> eventCallback) {
        Collection<EventCallback> callbacks = getEventCallbacks(eventClass);
        return callbacks.remove(eventCallback);
    }

    /**
     * Gets the event callbacks of a specific event type.
     *
     * @param eventClass the event class
     * @param <E>        the event type
     * @return all event callbacks for the specified type {@code eventClass}
     */
    @NotNull
    default <E extends Event> Collection<EventCallback> getEventCallbacks(@NotNull Class<E> eventClass) {
        return getEventCallbacksMap().computeIfAbsent(eventClass, clazz -> new CopyOnWriteArraySet<>());
    }

    /**
     * Gets a {@link Stream} containing all the {@link EventCallback}, no matter to which {@link Event} they are linked.
     *
     * @return a {@link Stream} containing all the callbacks
     */
    @NotNull
    default Stream<EventCallback> getEventCallbacks() {
        return getEventCallbacksMap().values().stream().flatMap(Collection::stream);
    }

    /**
     * Calls the specified {@link Event} with all the assigned {@link EventCallback}.
     * <p>
     * Events are always called in the current thread.
     *
     * @param eventClass the event class
     * @param event      the event object
     * @param <E>        the event type
     */
    default <E extends Event> void callEvent(@NotNull Class<E> eventClass, @NotNull E event) {

        // Global listeners
        if (!(this instanceof GlobalEventHandler)) {
            final GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
            runEvent(globalEventHandler.getEventCallbacks(eventClass), event);
        }

        // Local listeners
        final Collection<EventCallback> eventCallbacks = getEventCallbacks(eventClass);
        runEvent(eventCallbacks, event);

        // Call the same event for the current entity instance
        if (this instanceof Entity) {
            final Instance instance = ((Entity) this).getInstance();
            if (instance != null) {
                runEvent(instance.getEventCallbacks(eventClass), event);
            }
        }
    }

    /**
     * Calls a {@link CancellableEvent} and execute {@code successCallback} if the {@link Event} is not cancelled.
     * <p>
     * Does call {@link #callEvent(Class, Event)} internally.
     *
     * @param eventClass      the event class
     * @param event           the event object
     * @param successCallback the callback called when the event is not cancelled
     * @param <E>             the event type
     * @see #callEvent(Class, Event)
     */
    default <E extends Event & CancellableEvent> void callCancellableEvent(@NotNull Class<E> eventClass,
                                                                           @NotNull E event,
                                                                           @NotNull Runnable successCallback) {
        callEvent(eventClass, event);
        if (!event.isCancelled()) {
            successCallback.run();
        }
    }

    private <E extends Event> void runEvent(@NotNull Collection<EventCallback> eventCallbacks, @NotNull E event) {
        for (EventCallback<E> eventCallback : eventCallbacks) {
            eventCallback.run(event);
        }
    }

}
