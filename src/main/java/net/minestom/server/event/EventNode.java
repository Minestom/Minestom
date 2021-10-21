package net.minestom.server.event;

import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagReadable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Represents a single node in an event graph.
 * <p>
 * A node may contain any number of children and/or listeners. When an event is called,
 * the node will filter it based on the parameters given at creation and then propagate
 * it down to child nodes and listeners if it passes.
 *
 * @param <T> The event type accepted by this node
 */
public sealed interface EventNode<T extends Event> permits EventNodeImpl {

    /**
     * Creates an event node which accepts any event type with no filtering.
     *
     * @param name The name of the node
     * @return An event node with no filtering
     */
    @Contract(value = "_ -> new", pure = true)
    static @NotNull EventNode<Event> all(@NotNull String name) {
        return type(name, EventFilter.ALL);
    }

    /**
     * Creates an event node which accepts any event of the given type. The type is provided
     * by the {@link EventFilter}.
     * <p>
     * For example, you could create an event filter which only accepts player events with the following
     * <p><pre>
     * var playerEventNode = EventNode.type("demo", EventFilter.PLAYER);
     * </pre>
     *
     * @param name   The name of the event node
     * @param filter The event type filter to apply
     * @param <E>    The resulting event type of the node
     * @return A node with just an event type filter
     */
    @Contract(value = "_, _ -> new", pure = true)
    static <E extends Event, V> @NotNull EventNode<E> type(@NotNull String name,
                                                           @NotNull EventFilter<E, V> filter) {
        return create(name, filter, null);
    }

    /**
     * Creates an event node which accepts any event of the given type which passes
     * the provided condition. The condition is based on the event object itself.
     * <p>
     * For example, you could create an event filter which only accepts player events
     * where the player is in the pos x/z quadrant of the world.
     * <p><pre>{@code
     * var playerInPosXZNode = EventNode.event("abc", EventFilter.PLAYER, event -> {
     *     var position = event.getPlayer().getPosition();
     *     return position.getX() > 0 && position.getZ() > 0;
     * });
     * }</pre>
     *
     * @param name      The name of the event node
     * @param filter    The event type filter to apply
     * @param predicate The event condition
     * @param <E>       The resulting event type of the node
     * @return A node with an event type filter as well as a condition on the event.
     */
    @Contract(value = "_, _, _ -> new", pure = true)
    static <E extends Event, V> @NotNull EventNode<E> event(@NotNull String name,
                                                            @NotNull EventFilter<E, V> filter,
                                                            @NotNull Predicate<E> predicate) {
        return create(name, filter, (e, h) -> predicate.test(e));
    }

    /**
     * Creates an event node which accepts any event of the given type which passes
     * the provided condition. The condition is based on the event object as well as
     * the event handler type defined in the {@link EventFilter}.
     * <p>
     * For example, you could create an event filter which only accepts player events
     * where the player is in the pos x/z quadrant of the world.
     * <p><pre>{@code
     * var playerInPosXZNode = EventNode.type("abc", EventFilter.PLAYER, (event, player) -> {
     *     var position = player.getPosition();
     *     return position.getX() > 0 && position.getZ() > 0;
     * });
     * }</pre>
     *
     * @param name      The name of the event node
     * @param filter    The event type filter to apply
     * @param predicate The event condition
     * @param <E>       The resulting event type of the node
     * @param <V>       The handler type of the event filter
     * @return A node with an event type filter as well as a condition on the event.
     */
    @Contract(value = "_, _, _ -> new", pure = true)
    static <E extends Event, V> @NotNull EventNode<E> type(@NotNull String name,
                                                           @NotNull EventFilter<E, V> filter,
                                                           @NotNull BiPredicate<E, V> predicate) {
        return create(name, filter, predicate);
    }

    /**
     * Creates an event node which accepts any event of the given type which passes
     * the provided condition. The condition is based on the event handler defined
     * by the {@link EventFilter}.
     * <p>
     * For example, you could create an event filter which only accepts player events
     * where the player is in creative mode.
     * <p><pre>
     * var playerIsCreative = EventNode.value("abc", EventFilter.PLAYER, Player::isCreative);
     * </pre>
     *
     * @param name      The name of the event node
     * @param filter    The event type filter to apply
     * @param predicate The event condition
     * @param <E>       The resulting event type of the node
     * @param <V>       The handler type of the event filter
     * @return A node with an event type filter as well as a condition on the event.
     */
    @Contract(value = "_, _, _ -> new", pure = true)
    static <E extends Event, V> @NotNull EventNode<E> value(@NotNull String name,
                                                            @NotNull EventFilter<E, V> filter,
                                                            @NotNull Predicate<V> predicate) {
        return create(name, filter, (e, h) -> predicate.test(h));
    }

    /**
     * Creates an event node which accepts any event of the given type which has a handler who
     * has the given tag.
     * <p>
     * The {@link EventFilter}'s resulting event type must be {@link TagReadable}.
     *
     * @param name   The name of the event node
     * @param filter The event type filter to apply
     * @param tag    The tag which must be contained on the event handler
     * @param <E>    The resulting event type of the node
     * @return A node with an event type filter as well as a handler with the provided tag
     */
    @Contract(value = "_, _, _ -> new", pure = true)
    static <E extends Event> @NotNull EventNode<E> tag(@NotNull String name,
                                                       @NotNull EventFilter<E, ? extends TagReadable> filter,
                                                       @NotNull Tag<?> tag) {
        return create(name, filter, (e, h) -> h.hasTag(tag));
    }

    /**
     * Creates an event node which accepts any event of the given type which has a handler who
     * has an applicable tag. An applicable tag means that it passes the given condition.
     *
     * @param name     The name of the event node
     * @param filter   The event type filter to apply
     * @param tag      The tag which must be contained on the event handler
     * @param consumer The condition to test against the tag, if it exists.
     * @param <E>      The resulting event type of the node
     * @return A node with an event type filter as well as a handler with the provided tag
     */
    @Contract(value = "_, _, _, _ -> new", pure = true)
    static <E extends Event, V> @NotNull EventNode<E> tag(@NotNull String name,
                                                          @NotNull EventFilter<E, ? extends TagReadable> filter,
                                                          @NotNull Tag<V> tag,
                                                          @NotNull Predicate<@Nullable V> consumer) {
        return create(name, filter, (e, h) -> consumer.test(h.getTag(tag)));
    }

    private static <E extends Event, V> EventNode<E> create(@NotNull String name,
                                                            @NotNull EventFilter<E, V> filter,
                                                            @Nullable BiPredicate<E, V> predicate) {
        //noinspection unchecked
        return new EventNodeImpl<>(name, filter, predicate != null ? (e, o) -> predicate.test(e, (V) o) : null);
    }

    /**
     * Calls an event starting from this node.
     *
     * @param event the event to call
     */
    default void call(@NotNull T event) {
        //noinspection unchecked
        getHandle((Class<T>) event.getClass()).call(event);
    }

    /**
     * Gets the handle of an event type.
     *
     * @param handleType the handle type
     * @param <E>        the event type
     * @return the handle linked to {@code handleType}
     */
    @ApiStatus.Experimental
    <E extends T> @NotNull ListenerHandle<E> getHandle(@NotNull Class<E> handleType);

    /**
     * Execute a cancellable event with a callback to execute if the event is successful.
     * Event conditions and propagation is the same as {@link #call(Event)}.
     *
     * @param event           The event to execute
     * @param successCallback A callback if the event is not cancelled
     */
    default void callCancellable(@NotNull T event, @NotNull Runnable successCallback) {
        call(event);
        if (!(event instanceof CancellableEvent) || !((CancellableEvent) event).isCancelled()) {
            successCallback.run();
        }
    }

    @Contract(pure = true)
    @NotNull Class<T> getEventType();

    @Contract(pure = true)
    @NotNull String getName();

    @Contract(pure = true)
    int getPriority();

    @Contract(value = "_ -> this")
    @NotNull EventNode<T> setPriority(int priority);

    @Contract(pure = true)
    @Nullable EventNode<? super T> getParent();

    /**
     * Returns an unmodifiable view of the children in this node.
     *
     * @see #addChild(EventNode)
     * @see #removeChild(EventNode)
     */
    @Contract(pure = true)
    @NotNull Set<@NotNull EventNode<T>> getChildren();

    /**
     * Locates all child nodes with the given name and event type recursively starting at this node.
     *
     * @param name      The event node name to filter for
     * @param eventType The event node type to filter for
     * @return All matching event nodes
     */
    @Contract(pure = true)
    <E extends T> @NotNull List<EventNode<E>> findChildren(@NotNull String name, Class<E> eventType);

    /**
     * Locates all child nodes with the given name and event type recursively starting at this node.
     *
     * @param name The event name to filter for
     * @return All matching event nodes
     */
    @Contract(pure = true)
    default @NotNull List<EventNode<T>> findChildren(@NotNull String name) {
        return findChildren(name, getEventType());
    }

    /**
     * Replaces all children matching the given name and type recursively starting from this node.
     * <p>
     * Node: The callee may not be replaced by this call.
     *
     * @param name      The event name to filter for
     * @param eventType The event node type to filter for
     * @param eventNode The replacement node
     */
    <E extends T> void replaceChildren(@NotNull String name, @NotNull Class<E> eventType, @NotNull EventNode<E> eventNode);

    /**
     * Replaces all children matching the given name and type recursively starting from this node.
     * <p>
     * Node: The callee may not be replaced by this call.
     *
     * @param name      The node name to filter for
     * @param eventNode The replacement node
     */
    default void replaceChildren(@NotNull String name, @NotNull EventNode<T> eventNode) {
        replaceChildren(name, getEventType(), eventNode);
    }

    /**
     * Recursively removes children with the given name and type starting at this node.
     *
     * @param name      The node name to filter for
     * @param eventType The node type to filter for
     */
    void removeChildren(@NotNull String name, @NotNull Class<? extends T> eventType);

    /**
     * Recursively removes children with the given name starting at this node.
     *
     * @param name The node name to filter for
     */
    default void removeChildren(@NotNull String name) {
        removeChildren(name, getEventType());
    }

    /**
     * Directly adds a child node to this node.
     *
     * @param child The child to add
     * @return this, can be used for chaining
     */
    @Contract(value = "_ -> this")
    @NotNull EventNode<T> addChild(@NotNull EventNode<? extends T> child);

    /**
     * Directly removes the given child from this node.
     *
     * @param child The child to remove
     * @return this, can be used for chaining
     */
    @Contract(value = "_ -> this")
    @NotNull EventNode<T> removeChild(@NotNull EventNode<? extends T> child);

    @Contract(value = "_ -> this")
    @NotNull EventNode<T> addListener(@NotNull EventListener<? extends T> listener);

    @Contract(value = "_, _ -> this")
    default <E extends T> @NotNull EventNode<T> addListener(@NotNull Class<E> eventType, @NotNull Consumer<@NotNull E> listener) {
        return addListener(EventListener.of(eventType, listener));
    }

    @Contract(value = "_ -> this")
    @NotNull EventNode<T> removeListener(@NotNull EventListener<? extends T> listener);

    /**
     * Maps a specific object to a node.
     * <p>
     * Be aware that such structure have huge performance penalty as they will
     * always require a map lookup. Use only at last resort.
     *
     * @param node  the node to map
     * @param value the mapped value
     */
    @ApiStatus.Experimental
    void map(@NotNull EventNode<? extends T> node, @NotNull Object value);

    /**
     * Undo {@link #map(EventNode, Object)}
     *
     * @param value the value to unmap
     * @return true if the value has been unmapped, false if nothing happened
     */
    @ApiStatus.Experimental
    boolean unmap(@NotNull Object value);

    @ApiStatus.Experimental
    void register(@NotNull EventBinding<? extends T> binding);

    @ApiStatus.Experimental
    void unregister(@NotNull EventBinding<? extends T> binding);
}
