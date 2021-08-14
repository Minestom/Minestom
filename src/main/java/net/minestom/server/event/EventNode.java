package net.minestom.server.event;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.trait.*;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagReadable;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
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
public class EventNode<T extends Event> {

    /**
     * Creates an event node which accepts any event type with no filtering.
     *
     * @param name The name of the node
     * @return An event node with no filtering
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull EventNode<Event> all(@NotNull String name) {
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
    public static <E extends Event, V> @NotNull EventNode<E> type(@NotNull String name,
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
    public static <E extends Event, V> @NotNull EventNode<E> event(@NotNull String name,
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
    public static <E extends Event, V> @NotNull EventNode<E> type(@NotNull String name,
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
    public static <E extends Event, V> @NotNull EventNode<E> value(@NotNull String name,
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
    public static <E extends Event> @NotNull EventNode<E> tag(@NotNull String name,
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
    public static <E extends Event, V> @NotNull EventNode<E> tag(@NotNull String name,
                                                                 @NotNull EventFilter<E, ? extends TagReadable> filter,
                                                                 @NotNull Tag<V> tag,
                                                                 @NotNull Predicate<@Nullable V> consumer) {
        return create(name, filter, (e, h) -> consumer.test(h.getTag(tag)));
    }

    public static <E extends Event, V> @NotNull Mapped<E, V> mapped(@NotNull String name,
                                                                    @NotNull EventFilter<E, V> filter,
                                                                    @NotNull V value) {
        return new Mapped<>(name, filter, value);
    }

    private static <E extends Event, V> EventNode<E> create(@NotNull String name,
                                                            @NotNull EventFilter<E, V> filter,
                                                            @Nullable BiPredicate<E, V> predicate) {
        return new EventNode<>(name, filter, predicate != null ? (e, o) -> predicate.test(e, (V) o) : null);
    }

    private static final Map<Class<? extends Event>, List<Function<Event, Object>>> HANDLER_SUPPLIERS = new ConcurrentHashMap<>();
    private static final Object GLOBAL_CHILD_LOCK = new Object();
    private final Object lock = new Object();

    private final Map<Class<? extends T>, ListenerEntry<T>> listenerMap = new ConcurrentHashMap<>();
    private final Set<EventNode<T>> children = new CopyOnWriteArraySet<>();
    private final Map<Object, EventNode<T>> mappedNode = new ConcurrentHashMap<>();

    protected final String name;
    protected final EventFilter<T, ?> filter;
    protected final BiPredicate<T, Object> predicate;
    protected final Class<T> eventType;
    private volatile int priority;
    private volatile EventNode<? super T> parent;

    protected EventNode(@NotNull String name,
                        @NotNull EventFilter<T, ?> filter,
                        @Nullable BiPredicate<T, Object> predicate) {
        this.name = name;
        this.filter = filter;
        this.predicate = predicate;
        this.eventType = filter.getEventType();
    }

    /**
     * Condition to enter the node.
     *
     * @param event the called event
     * @return true to enter the node, false otherwise
     */
    protected boolean condition(@NotNull T event) {
        if (predicate == null)
            return true;
        final var value = filter.getHandler(event);
        try {
            return predicate.test(event, value);
        } catch (Exception e) {
            MinecraftServer.getExceptionManager().handleException(e);
            return false;
        }
    }

    /**
     * Executes the given event on this node. The event must pass all conditions before
     * it will be forwarded to the listeners.
     * <p>
     * Calling an event on a node will execute all child nodes, however, an event may be
     * called anywhere on the event graph and it will propagate down from there only.
     *
     * @param event the event to execute
     */
    public void call(@NotNull T event) {
        final var eventClass = event.getClass();
        if (!eventType.isAssignableFrom(eventClass)) {
            // Invalid event type
            return;
        }
        if (!condition(event)) {
            // Cancelled by superclass
            return;
        }
        // Mapped listeners
        if (!mappedNode.isEmpty()) {
            // Check mapped listeners for each individual event handler
            getEventMapping(eventClass).forEach(function -> {
                final var handler = function.apply(event);
                final var map = mappedNode.get(handler);
                if (map != null) map.call(event);
            });
        }
        // Process listener list
        final var entry = listenerMap.get(eventClass);
        if (entry == null) {
            // No listener nor children
            return;
        }

        final var listeners = entry.listeners;
        if (!listeners.isEmpty()) {
            for (EventListener<T> listener : listeners) {
                EventListener.Result result;
                try {
                    result = listener.run(event);
                } catch (Exception e) {
                    result = EventListener.Result.EXCEPTION;
                    MinecraftServer.getExceptionManager().handleException(e);
                }
                if (result == EventListener.Result.EXPIRED) {
                    listeners.remove(listener);
                }
            }
        }
        // Process children
        if (entry.childCount > 0) {
            this.children.stream()
                    .sorted(Comparator.comparing(EventNode::getPriority))
                    .forEach(child -> child.call(event));
        }
    }

    /**
     * Execute a cancellable event with a callback to execute if the event is successful.
     * Event conditions and propagation is the same as {@link #call(Event)}.
     *
     * @param event           The event to execute
     * @param successCallback A callback if the event is not cancelled
     */
    public void callCancellable(@NotNull T event, @NotNull Runnable successCallback) {
        call(event);
        if (!(event instanceof CancellableEvent) || !((CancellableEvent) event).isCancelled()) {
            successCallback.run();
        }
    }

    @Contract(pure = true)
    public @NotNull String getName() {
        return name;
    }

    @Contract(pure = true)
    public int getPriority() {
        return priority;
    }

    @Contract(value = "_ -> this")
    public @NotNull EventNode<T> setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    @Contract(pure = true)
    public @Nullable EventNode<? super T> getParent() {
        return parent;
    }

    /**
     * Returns an unmodifiable view of the children in this node.
     *
     * @see #addChild(EventNode)
     * @see #removeChild(EventNode)
     */
    @Contract(pure = true)
    public @NotNull Set<@NotNull EventNode<T>> getChildren() {
        return Collections.unmodifiableSet(children);
    }

    /**
     * Locates all child nodes with the given name and event type recursively starting at this node.
     *
     * @param name      The event node name to filter for
     * @param eventType The event node type to filter for
     * @return All matching event nodes
     */
    @Contract(pure = true)
    public <E extends T> @NotNull List<EventNode<E>> findChildren(@NotNull String name, Class<E> eventType) {
        if (children.isEmpty()) {
            return Collections.emptyList();
        }
        synchronized (GLOBAL_CHILD_LOCK) {
            List<EventNode<E>> result = new ArrayList<>();
            for (EventNode<T> child : children) {
                if (EventNode.equals(child, name, eventType)) {
                    result.add((EventNode<E>) child);
                }
                result.addAll(child.findChildren(name, eventType));
            }
            return result;
        }
    }

    /**
     * Locates all child nodes with the given name and event type recursively starting at this node.
     *
     * @param name The event name to filter for
     * @return All matching event nodes
     */
    @Contract(pure = true)
    public @NotNull List<EventNode<T>> findChildren(@NotNull String name) {
        return findChildren(name, eventType);
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
    public <E extends T> void replaceChildren(@NotNull String name, @NotNull Class<E> eventType, @NotNull EventNode<E> eventNode) {
        if (children.isEmpty()) {
            return;
        }
        synchronized (GLOBAL_CHILD_LOCK) {
            for (EventNode<T> child : children) {
                if (EventNode.equals(child, name, eventType)) {
                    removeChild(child);
                    addChild(eventNode);
                    continue;
                }
                child.replaceChildren(name, eventType, eventNode);
            }
        }
    }

    /**
     * Replaces all children matching the given name and type recursively starting from this node.
     * <p>
     * Node: The callee may not be replaced by this call.
     *
     * @param name      The node name to filter for
     * @param eventNode The replacement node
     */
    public void replaceChildren(@NotNull String name, @NotNull EventNode<T> eventNode) {
        replaceChildren(name, eventType, eventNode);
    }

    /**
     * Recursively removes children with the given name and type starting at this node.
     *
     * @param name      The node name to filter for
     * @param eventType The node type to filter for
     */
    public void removeChildren(@NotNull String name, @NotNull Class<? extends T> eventType) {
        if (children.isEmpty()) {
            return;
        }
        synchronized (GLOBAL_CHILD_LOCK) {
            for (EventNode<T> child : children) {
                if (EventNode.equals(child, name, eventType)) {
                    removeChild(child);
                    continue;
                }
                child.removeChildren(name, eventType);
            }
        }
    }

    /**
     * Recursively removes children with the given name starting at this node.
     *
     * @param name The node name to filter for
     */
    public void removeChildren(@NotNull String name) {
        removeChildren(name, eventType);
    }

    /**
     * Directly adds a child node to this node.
     *
     * @param child The child to add
     * @return this, can be used for chaining
     */
    @Contract(value = "_ -> this")
    public @NotNull EventNode<T> addChild(@NotNull EventNode<? extends T> child) {
        synchronized (GLOBAL_CHILD_LOCK) {
            Check.stateCondition(child.parent != null, "Node already has a parent");
            Check.stateCondition(Objects.equals(parent, child), "Cannot have a child as parent");
            final boolean result = this.children.add((EventNode<T>) child);
            if (result) {
                child.parent = this;
                // Increase listener count
                synchronized (lock) {
                    child.listenerMap.forEach((eventClass, eventListeners) -> {
                        final var entry = child.listenerMap.get(eventClass);
                        if (entry == null)
                            return;
                        final int childCount = entry.listeners.size() + entry.childCount;
                        increaseChildListenerCount(eventClass, childCount);
                    });
                }
            }
        }
        return this;
    }

    /**
     * Directly removes the given child from this node.
     *
     * @param child The child to remove
     * @return this, can be used for chaining
     */
    @Contract(value = "_ -> this")
    public @NotNull EventNode<T> removeChild(@NotNull EventNode<? extends T> child) {
        synchronized (GLOBAL_CHILD_LOCK) {
            final boolean result = this.children.remove(child);
            if (result) {
                child.parent = null;
                // Decrease listener count
                synchronized (lock) {
                    child.listenerMap.forEach((eventClass, eventListeners) -> {
                        final var entry = child.listenerMap.get(eventClass);
                        if (entry == null)
                            return;
                        final int childCount = entry.listeners.size() + entry.childCount;
                        decreaseChildListenerCount(eventClass, childCount);
                    });
                }
            }
        }
        return this;
    }

    @Contract(value = "_ -> this")
    public @NotNull EventNode<T> addListener(@NotNull EventListener<? extends T> listener) {
        synchronized (GLOBAL_CHILD_LOCK) {
            final var eventType = listener.getEventType();
            var entry = listenerMap.computeIfAbsent(eventType, aClass -> new ListenerEntry<>());
            entry.listeners.add((EventListener<T>) listener);
            if (parent != null) {
                synchronized (parent.lock) {
                    parent.increaseChildListenerCount(eventType, 1);
                }
            }
        }
        return this;
    }

    @Contract(value = "_, _ -> this")
    public <E extends T> @NotNull EventNode<T> addListener(@NotNull Class<E> eventType, @NotNull Consumer<@NotNull E> listener) {
        return addListener(EventListener.of(eventType, listener));
    }

    @Contract(value = "_ -> this")
    public @NotNull EventNode<T> removeListener(@NotNull EventListener<? extends T> listener) {
        synchronized (GLOBAL_CHILD_LOCK) {
            final var eventType = listener.getEventType();
            var entry = listenerMap.get(eventType);
            if (entry == null)
                return this;
            var listeners = entry.listeners;
            final boolean removed = listeners.remove(listener);
            if (removed && parent != null) {
                synchronized (parent.lock) {
                    parent.decreaseChildListenerCount(eventType, 1);
                }
            }
        }
        return this;
    }

    public <E extends T, V> void map(@NotNull Mapped<E, V> map) {
        this.mappedNode.put(map.value, (EventNode<T>) map);
    }

    public <I> void addInter(@NotNull EventInterface<I> inter, @NotNull I value) {
        inter.mapped.forEach((eventType, consumer) -> {
            // TODO cache so listeners can be removed from the EventInterface
            addListener((EventListener<? extends T>) EventListener.builder(eventType).handler(event -> consumer.accept(value, event)).build());
        });
    }

    private void increaseChildListenerCount(Class<? extends T> eventClass, int count) {
        var entry = listenerMap.computeIfAbsent(eventClass, aClass -> new ListenerEntry<>());
        ListenerEntry.addAndGet(entry, count);
        if (parent != null) {
            parent.increaseChildListenerCount(eventClass, count);
        }
    }

    private void decreaseChildListenerCount(Class<? extends T> eventClass, int count) {
        var entry = listenerMap.computeIfAbsent(eventClass, aClass -> new ListenerEntry<>());
        final int result = ListenerEntry.addAndGet(entry, -count);
        if (result == 0 && entry.listeners.isEmpty()) {
            this.listenerMap.remove(eventClass);
        } else if (result < 0) {
            throw new IllegalStateException("Something wrong happened, listener count: " + result);
        }
        if (parent != null) {
            parent.decreaseChildListenerCount(eventClass, count);
        }
    }

    private static boolean equals(EventNode<?> node, String name, Class<?> eventType) {
        final boolean nameCheck = node.getName().equals(name);
        final boolean typeCheck = eventType.isAssignableFrom(node.eventType);
        return nameCheck && typeCheck;
    }

    // Returns a list of (event->object) functions used to retrieve handler.
    // For example `PlayerUseItemEvent` should return a function to retrieve the player,
    // and another for the item.
    // All event trait are currently hardcoded.
    private static List<Function<Event, Object>> getEventMapping(Class<? extends Event> eventClass) {
        return HANDLER_SUPPLIERS.computeIfAbsent(eventClass, clazz -> {
            List<Function<Event, Object>> result = new ArrayList<>();
            if (EntityEvent.class.isAssignableFrom(clazz)) {
                result.add(e -> ((EntityEvent) e).getEntity());
            } else if (PlayerEvent.class.isAssignableFrom(clazz)) {
                result.add(e -> ((PlayerEvent) e).getPlayer());
            } else if (ItemEvent.class.isAssignableFrom(clazz)) {
                result.add(e -> ((ItemEvent) e).getItemStack());
            } else if (InstanceEvent.class.isAssignableFrom(clazz)) {
                result.add(e -> ((InstanceEvent) e).getInstance());
            } else if (InventoryEvent.class.isAssignableFrom(clazz)) {
                result.add(e -> ((InventoryEvent) e).getInventory());
            } else if (BlockEvent.class.isAssignableFrom(clazz)) {
                result.add(e -> ((BlockEvent) e).getBlock());
            }
            return result;
        });
    }

    private static class ListenerEntry<T extends Event> {
        private static final AtomicIntegerFieldUpdater<ListenerEntry> CHILD_UPDATER =
                AtomicIntegerFieldUpdater.newUpdater(ListenerEntry.class, "childCount");

        List<EventListener<T>> listeners = new CopyOnWriteArrayList<>();
        volatile int childCount;

        private static int addAndGet(ListenerEntry<?> entry, int add) {
            return CHILD_UPDATER.addAndGet(entry, add);
        }
    }

    public static final class Mapped<T extends Event, V> extends EventNode<T> {
        private final V value;

        Mapped(@NotNull String name, @NotNull EventFilter<T, ?> filter, @NotNull V value) {
            super(name, filter, null);
            this.value = value;
        }
    }
}
