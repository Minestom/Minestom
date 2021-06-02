package net.minestom.server.event;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class EventNode<T extends Event> {

    private final String name = "debug";
    private final List<EventListener<T>> listeners = new CopyOnWriteArrayList<>();
    private final List<EventNode<T>> children = new CopyOnWriteArrayList<>();

    private static final EventNode<Event> EMPTY = new EventNode<>() {
        @Override
        protected boolean isValid(@NotNull Event event) {
            return true;
        }
    };

    public static EventNode<Event> create() {
        return EMPTY;
    }

    public static <E extends Event> EventNode<E> conditional(@NotNull Class<E> type,
                                                             @NotNull Predicate<E> predicate) {
        return new EventNodeConditional<>(predicate);
    }

    public static <E extends Event> EventNode<E> conditional(@NotNull Class<E> eventType) {
        return conditional(eventType, t -> true);
    }

    protected abstract boolean isValid(@NotNull T event);

    public void call(@NotNull T event) {
        if (!isValid(event))
            return;
        this.listeners.forEach(eventListener -> eventListener.getCombined().accept(event));
        this.children.forEach(eventNode -> eventNode.call(event));
    }

    public void addChild(@NotNull EventNode<? extends T> child) {
        this.children.add((EventNode<T>) child);
    }

    public void addListener(@NotNull EventListener<? extends T> listener) {
        this.listeners.add((EventListener<T>) listener);
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
