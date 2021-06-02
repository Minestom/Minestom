package net.minestom.server.event;

import net.minestom.server.event.handler.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public interface EventNode<T extends Event> {

    static <E extends Event> EventNode<E> create(@NotNull Class<E> type) {
        return new EventNodeImpl<>(type);
    }

    static EventNode<Event> create() {
        return create(Event.class);
    }

    static <E extends Event> EventNode<E> conditional(@NotNull Class<E> type,
                                                      @NotNull Predicate<E> predicate) {
        return new EventNodeConditional<>(type, predicate);
    }

    static <E extends Event, H extends EventHandler> EventNode<E> map(@NotNull Class<E> eventType,
                                                                      @NotNull Class<H> handlerType,
                                                                      @NotNull Function<E, H> handlerGetter) {
        return new EventNodeMap<>(eventType, handlerGetter);
    }

    void call(@NotNull T event);

    void addChild(@NotNull EventNode<? extends T> child);

    void addListener(@NotNull EventListener<? extends T> listener);

    default <E extends T> void addListener(@NotNull Class<E> eventClass, @NotNull Consumer<@NotNull E> listener) {
        addListener(EventListener.of(eventClass).handler(listener).build());
    }

    @NotNull String getName();

    @NotNull List<@NotNull EventNode<T>> getChildren();
}
