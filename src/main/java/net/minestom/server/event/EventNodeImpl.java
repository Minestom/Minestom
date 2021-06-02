package net.minestom.server.event;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

class EventNodeImpl<T extends Event, H> implements EventNode<T> {

    private final String name = "debug";

    private final Map<Class<? extends T>, List<EventListener<T>>> listenerMap = new ConcurrentHashMap<>();
    private final Map<Object, RedirectionEntry<T>> redirectionMap = new ConcurrentHashMap<>();
    private final List<EventNode<T>> children = new CopyOnWriteArrayList<>();

    protected final EventFilter<T, H> filter;

    protected EventNodeImpl(EventFilter<T, H> filter) {
        this.filter = filter;
    }

    /**
     * Condition to enter the node.
     *
     * @param event the called event
     * @return true to enter the node, false otherwise
     */
    protected boolean condition(@NotNull T event) {
        return true;
    }

    @Override
    public void call(@NotNull T event) {
        if (!filter.getEventType().isAssignableFrom(event.getClass())) {
            // Invalid event type
            return;
        }
        if (!condition(event)) {
            // Cancelled by superclass
            return;
        }
        // Process redirection
        final H handler = filter.getHandler(event);
        final var entry = redirectionMap.get(handler);
        if (entry != null) {
            entry.node.call(event);
        }
        // Process listener list
        final var listeners = listenerMap.get(event.getClass());
        if (listeners != null && !listeners.isEmpty()) {
            listeners.forEach(listener -> {
                final EventListener.Result result = listener.run(event);
                if (result == EventListener.Result.EXPIRED) {
                    listeners.remove(listener);
                }
            });
        }
        // Process children
        this.children.forEach(eventNode -> eventNode.call(event));
    }

    @Override
    public void addChild(@NotNull EventNode<? extends T> child) {
        this.children.add((EventNode<T>) child);
    }

    @Override
    public void removeChild(@NotNull EventNode<? extends T> child) {
        this.children.remove(child);
    }

    @Override
    public void addListener(@NotNull EventListener<? extends T> listener) {
        this.listenerMap.computeIfAbsent(listener.getEventType(), aClass -> new CopyOnWriteArrayList<>())
                .add((EventListener<T>) listener);
    }

    @Override
    public void removeListener(@NotNull EventListener<? extends T> listener) {
        var listeners = listenerMap.get(listener.getEventType());
        if (listeners == null || listeners.isEmpty())
            return;
        listeners.remove(listener);
    }

    @Override
    public <E extends T, V> void map(@NotNull EventFilter<E, V> filter, @NotNull V value, @NotNull EventNode<E> node) {
        RedirectionEntry<E> entry = new RedirectionEntry<>();
        entry.filter = filter;
        entry.node = node;
        this.redirectionMap.put(value, (RedirectionEntry<T>) entry);
    }

    @Override
    public void removeMap(@NotNull Object value) {
        this.redirectionMap.remove(value);
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull List<@NotNull EventNode<T>> getChildren() {
        return Collections.unmodifiableList(children);
    }

    private static class RedirectionEntry<E extends Event> {
        EventFilter<E, ?> filter;
        EventNode<E> node;
    }
}
