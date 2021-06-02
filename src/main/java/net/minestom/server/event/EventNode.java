package net.minestom.server.event;

import net.minestom.server.event.handler.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class EventNode<T extends Event> {

    private final String name = "debug";
    private final List<EventListener<? extends T>> listeners = new CopyOnWriteArrayList<>();
    private final List<EventNode<? extends T>> children = new CopyOnWriteArrayList<>();
    private final Predicate<T> condition = t -> true;

    private EventNode() {
    }

    public static EventNode<Event> create() {
        return new EventNode<>();
    }

    public static <E extends Event> EventNode<E> conditional(@NotNull Class<E> type,
                                                             @NotNull Predicate<E> predicate) {
        return new EventNode<>();
    }

    public static <E extends Event> EventNode<E> conditional(@NotNull Class<E> eventType) {
        return conditional(eventType, t -> true);
    }

    public static <E extends Event> EventNode<E> unique(@NotNull Class<E> eventType,
                                                        @NotNull EventHandler handler) {
        return new EventNode<>();
    }

    public void addChild(@NotNull EventNode<? extends T> child) {
        this.children.add(child);
    }

    public void addListener(@NotNull EventListener<? extends T> listener) {
        this.listeners.add(listener);
    }

    public <E extends T> void addListener(@NotNull Class<E> eventClass, @NotNull Consumer<@NotNull E> listener) {
        addListener(EventListener.of(eventClass).handler(listener).build());
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull List<@NotNull EventNode<? extends T>> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public @NotNull Predicate<@NotNull T> getCondition() {
        return condition;
    }
}
