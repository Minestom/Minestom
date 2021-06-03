package net.minestom.server.event;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagReadable;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class EventNode<T extends Event> {

    public static <E extends Event, V> EventNode<E> type(@NotNull EventFilter<E, V> filter) {
        return new EventNode<>(filter, (e, v) -> true);
    }

    public static EventNode<Event> all() {
        return type(EventFilter.ALL);
    }

    public static <E extends Event, V> EventNode<E> predicate(@NotNull EventFilter<E, V> filter,
                                                              @NotNull BiPredicate<E, V> predicate) {
        return new EventNode<>(filter, (e, o) -> predicate.test(e, (V) o));
    }

    public static <E extends Event, V> EventNode<E> predicateEvent(@NotNull EventFilter<E, V> filter,
                                                                   @NotNull Predicate<E> predicate) {
        return predicate(filter, (e, h) -> predicate.test(e));
    }

    public static <E extends Event, V> EventNode<E> predicateValue(@NotNull EventFilter<E, V> filter,
                                                                   @NotNull Predicate<V> predicate) {
        return predicate(filter, (e, h) -> predicate.test(h));
    }

    public static <E extends Event, V extends TagReadable> EventNode<E> predicateTag(@NotNull EventFilter<E, V> filter,
                                                                                     @NotNull Tag<?> tag) {
        return predicate(filter, (e, h) -> h.hasTag(tag));
    }

    public static <E extends Event, V extends TagReadable, V2> EventNode<E> predicateTag(@NotNull EventFilter<E, V> filter,
                                                                                         @NotNull Tag<V2> tag,
                                                                                         @NotNull Predicate<@Nullable V2> consumer) {
        return predicate(filter, (e, h) -> consumer.test(h.getTag(tag)));
    }

    private final Map<Class<? extends T>, List<EventListener<T>>> listenerMap = new ConcurrentHashMap<>();
    private final Map<Object, EventNode<T>> redirectionMap = new ConcurrentHashMap<>();
    private final Set<EventNode<T>> children = new CopyOnWriteArraySet<>();

    protected final EventFilter<T, ?> filter;
    protected final BiPredicate<T, Object> predicate;
    private volatile String name = "unknown";

    // Tree data
    private static final Object GLOBAL_CHILD_LOCK = new Object();
    private volatile EventNode<? super T> parent;
    private final Object lock = new Object();
    private final Object2IntMap<Class<? extends T>> childEventMap = new Object2IntOpenHashMap<>();

    protected EventNode(EventFilter<T, ?> filter, BiPredicate<T, Object> predicate) {
        this.filter = filter;
        this.predicate = predicate;
    }

    /**
     * Condition to enter the node.
     *
     * @param event the called event
     * @return true to enter the node, false otherwise
     */
    protected boolean condition(@NotNull T event) {
        final var value = filter.getHandler(event);
        return predicate.test(event, value);
    }

    public void call(@NotNull T event) {
        final var eventClass = event.getClass();
        if (!filter.getEventType().isAssignableFrom(eventClass)) {
            // Invalid event type
            return;
        }
        if (!condition(event)) {
            // Cancelled by superclass
            return;
        }
        // Process redirection
        final Object handler = filter.getHandler(event);
        if (handler != null) {
            final var node = redirectionMap.get(handler);
            if (node != null) {
                node.call(event);
            }
        }
        // Process listener list
        final var listeners = listenerMap.get(eventClass);
        if (listeners != null && !listeners.isEmpty()) {
            listeners.forEach(listener -> {
                final EventListener.Result result = listener.run(event);
                if (result == EventListener.Result.EXPIRED) {
                    listeners.remove(listener);
                }
            });
        }
        // Process children
        synchronized (lock) {
            final int childCount = childEventMap.getInt(eventClass);
            if (childCount < 1) {
                // No listener in children
                return;
            }
        }
        this.children.forEach(eventNode -> eventNode.call(event));
    }

    public EventNode<T> addChild(@NotNull EventNode<? extends T> child) {
        synchronized (GLOBAL_CHILD_LOCK) {
            Check.stateCondition(Objects.equals(parent, child), "Cannot have a child as parent");
            final boolean result = this.children.add((EventNode<T>) child);
            if (result) {
                child.parent = this;
                // Increase listener count
                synchronized (lock) {
                    child.listenerMap.forEach((eventClass, eventListeners) -> {
                        final int childCount = eventListeners.size() + child.childEventMap.getInt(eventClass);
                        increaseListenerCount(eventClass, childCount);
                    });
                }
            }
        }
        return this;
    }

    public EventNode<T> removeChild(@NotNull EventNode<? extends T> child) {
        synchronized (GLOBAL_CHILD_LOCK) {
            final boolean result = this.children.remove(child);
            if (result) {
                child.parent = null;
                // Decrease listener count
                synchronized (lock) {
                    child.listenerMap.forEach((eventClass, eventListeners) -> {
                        final int childCount = eventListeners.size() + child.childEventMap.getInt(eventClass);
                        decreaseListenerCount(eventClass, childCount);
                    });
                }
            }
        }
        return this;
    }

    public EventNode<T> addListener(@NotNull EventListener<? extends T> listener) {
        synchronized (GLOBAL_CHILD_LOCK) {
            final var eventType = listener.getEventType();
            this.listenerMap.computeIfAbsent(eventType, aClass -> new CopyOnWriteArrayList<>())
                    .add((EventListener<T>) listener);
            if (parent != null) {
                synchronized (parent.lock) {
                    parent.increaseListenerCount(eventType, 1);
                }
            }
        }
        return this;
    }

    public EventNode<T> removeListener(@NotNull EventListener<? extends T> listener) {
        synchronized (GLOBAL_CHILD_LOCK) {
            final var eventType = listener.getEventType();
            var listeners = listenerMap.get(eventType);
            if (listeners == null || listeners.isEmpty())
                return this;
            final boolean removed = listeners.remove(listener);
            if (removed && parent != null) {
                synchronized (parent.lock) {
                    parent.decreaseListenerCount(eventType, 1);
                }
            }
        }
        return this;
    }

    public <E extends T> EventNode<T> addListener(@NotNull Class<E> eventType, @NotNull Consumer<@NotNull E> listener) {
        return addListener(EventListener.of(eventType, listener));
    }

    public <E extends T> EventNode<T> map(@NotNull Object value, @NotNull EventNode<E> node) {
        this.redirectionMap.put(value, (EventNode<T>) node);
        return this;
    }

    public EventNode<T> unmap(@NotNull Object value) {
        this.redirectionMap.remove(value);
        return this;
    }

    public @NotNull String getName() {
        return name;
    }

    public EventNode<T> setName(@NotNull String name) {
        this.name = name;
        return this;
    }

    public @NotNull Set<@NotNull EventNode<T>> getChildren() {
        return Collections.unmodifiableSet(children);
    }

    private void increaseListenerCount(Class<? extends T> eventClass, int count) {
        final int current = childEventMap.getInt(eventClass);
        final int result = current + count;
        this.childEventMap.put(eventClass, result);
    }

    private void decreaseListenerCount(Class<? extends T> eventClass, int count) {
        final int current = childEventMap.getInt(eventClass);
        final int result = current - count;
        if (result == 0) {
            this.childEventMap.removeInt(eventClass);
        } else if (result > 0) {
            this.childEventMap.put(eventClass, result);
        } else {
            throw new IllegalStateException("Something wrong happened, listener count: " + result);
        }
    }
}
