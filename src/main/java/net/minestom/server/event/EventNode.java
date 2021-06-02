package net.minestom.server.event;

import net.minestom.server.event.handler.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public interface EventNode<T extends Event> {

    static <E extends Event> EventNode<E> type(@NotNull EventFilter<E, EventHandler> filter) {
        return new EventNodeImpl<>(filter.getEventType());
    }

    static <E extends Event> EventNode<E> type(@NotNull Class<E> type) {
        return type(EventFilter.from(type));
    }

    static EventNode<Event> all() {
        return type(EventFilter.ALL);
    }

    static <E extends Event> EventNodeConditional<E> conditional(@NotNull Class<E> type,
                                                                 @NotNull Predicate<E> predicate) {
        return new EventNodeConditional<>(type, predicate);
    }

    static <E extends Event, H extends EventHandler> EventNodeList<E, H> list(@NotNull EventFilter<E, H> filter) {
        return new EventNodeList<>(filter);
    }

    static <E extends Event, H extends EventHandler> EventNodeList<E, H> list(@NotNull Class<E> eventType, EventFilter<? super E, H> parser) {
        return (EventNodeList<E, H>) list(parser);
    }

    static <E extends Event, H extends EventHandler> EventNodeList<E, H> list(@NotNull Class<E> eventType,
                                                                              @NotNull Class<H> handlerType,
                                                                              @NotNull Function<E, H> handlerGetter) {
        return list(EventFilter.from(eventType, handlerType, handlerGetter));
    }

    void call(@NotNull T event);

    void addChild(@NotNull EventNode<? extends T> child);

    void removeChild(@NotNull EventNode<? extends T> child);

    void addListener(@NotNull EventListener<? extends T> listener);

    void removeListener(@NotNull EventListener<? extends T> listener);

    default <E extends T> void addListener(@NotNull Class<E> eventClass, @NotNull Consumer<@NotNull E> listener) {
        addListener(EventListener.of(eventClass).handler(listener).build());
    }

    @NotNull String getName();

    @NotNull List<@NotNull EventNode<T>> getChildren();
}
