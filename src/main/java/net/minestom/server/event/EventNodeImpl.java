package net.minestom.server.event;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

class EventNodeImpl<T extends Event> implements EventNode<T> {

    private final String name = "debug";

    private final Map<Class<? extends T>, List<EventListener<T>>> listenerMap = new ConcurrentHashMap<>();
    private final List<EventNode<T>> children = new CopyOnWriteArrayList<>();

    protected final Class<T> type;

    protected EventNodeImpl(Class<T> type) {
        this.type = type;
    }

    protected boolean condition(@NotNull T event) {
        return true;
    }

    @Override
    public void call(@NotNull T event) {
        if (!type.isAssignableFrom(event.getClass())) {
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
                final EventListener.Result result = listener.executor.apply(event);
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
    public void addListener(@NotNull EventListener<? extends T> listener) {
        this.listenerMap.computeIfAbsent(listener.type, aClass -> new CopyOnWriteArrayList<>())
                .add((EventListener<T>) listener);
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
