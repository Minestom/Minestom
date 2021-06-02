package net.minestom.server.event;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class EventNode<T extends Event> {

    private final String name = "debug";

    private final Map<Class<? extends T>, List<EventListener<T>>> listenerMap = new ConcurrentHashMap<>();
    private final List<EventNode<T>> children = new CopyOnWriteArrayList<>();

    protected final Class<T> type;

    protected EventNode(Class<T> type) {
        this.type = type;
    }

    public static <E extends Event> EventNode<E> create(@NotNull Class<E> type) {
        return new EventNode<>(type);
    }

    public static EventNode<Event> create() {
        return create(Event.class);
    }

    public static <E extends Event> EventNode<E> conditional(@NotNull Class<E> type,
                                                             @NotNull Predicate<E> predicate) {
        return new EventNodeConditional<>(type, predicate);
    }

    protected boolean condition(@NotNull T event) {
        return true;
    }

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

    public void addChild(@NotNull EventNode<? extends T> child) {
        this.children.add((EventNode<T>) child);
    }

    public void addListener(@NotNull EventListener<? extends T> listener) {
        this.listenerMap.computeIfAbsent(listener.type, aClass -> new CopyOnWriteArrayList<>())
                .add((EventListener<T>) listener);
    }

    public <E extends T> void addListener(@NotNull Class<E> eventClass, @NotNull Consumer<@NotNull E> listener) {
        addListener(EventListener.of(eventClass).handler(listener).build());
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull List<@NotNull EventNode<T>> getChildren() {
        return Collections.unmodifiableList(children);
    }
}
