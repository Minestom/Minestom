package net.minestom.server.event;

import net.minestom.server.event.handler.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

class EventNodeImpl<T extends Event, H extends EventHandler> implements EventNode<T> {

    private final String name = "debug";

    private final Map<Class<? extends T>, List<EventListener<T>>> listenerMap = new ConcurrentHashMap<>();
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
        final var listeners = listenerMap.get(event.getClass());
        if (listeners != null && !listeners.isEmpty()) {
            listeners.forEach(listener -> {
                final EventListener.Result result = listener.run(event);
                if (result == EventListener.Result.EXPIRED) {
                    listeners.remove(listener);
                }
            });
        }
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
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull List<@NotNull EventNode<T>> getChildren() {
        return Collections.unmodifiableList(children);
    }
}
