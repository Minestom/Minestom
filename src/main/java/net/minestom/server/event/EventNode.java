package net.minestom.server.event;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public interface EventNode<T extends Event> {

    static <E extends Event> EventNode<E> type(@NotNull EventFilter<E, ?> filter) {
        return new EventNodeImpl<>(filter);
    }

    static EventNode<Event> all() {
        return type(EventFilter.ALL);
    }

    static <E extends Event, H> EventNodeConditional<E, H> conditional(@NotNull EventFilter<E, H> filter,
                                                                       @NotNull BiPredicate<E, H> predicate) {
        return new EventNodeConditional<>(filter, predicate);
    }

    static <E extends Event, H> EventNodeConditional<E, H> conditionalEvent(@NotNull EventFilter<E, H> filter,
                                                                            @NotNull Predicate<E> predicate) {
        return conditional(filter, (e, h) -> predicate.test(e));
    }

    static <E extends Event, H> EventNodeConditional<E, H> conditionalHandler(@NotNull EventFilter<E, H> filter,
                                                                              @NotNull Predicate<H> predicate) {
        return conditional(filter, (e, h) -> predicate.test(h));
    }

    static <E extends Event, H> EventNodeList<E, H> list(@NotNull EventFilter<E, H> filter) {
        return new EventNodeList<>(filter);
    }

    void call(@NotNull T event);

    void addChild(@NotNull EventNode<? extends T> child);

    void removeChild(@NotNull EventNode<? extends T> child);

    void addListener(@NotNull EventListener<? extends T> listener);

    void removeListener(@NotNull EventListener<? extends T> listener);

    <E extends T, V> void map(@NotNull EventFilter<E, V> filter, @NotNull V value, @NotNull EventNode<E> node);

    void removeMap(@NotNull Object value);

    @NotNull String getName();

    @NotNull List<@NotNull EventNode<T>> getChildren();
}
