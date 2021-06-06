package net.minestom.server.event;

import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagReadable;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class EventNode<T extends Event> {

    public static EventNode<Event> all(@NotNull String name) {
        return type(name, EventFilter.ALL);
    }

    public static <E extends Event, V> EventNode<E> type(@NotNull String name,
                                                         @NotNull EventFilter<E, V> filter,
                                                         @NotNull BiPredicate<E, V> predicate) {
        return create(name, filter, predicate);
    }

    public static <E extends Event, V> EventNode<E> type(@NotNull String name,
                                                         @NotNull EventFilter<E, V> filter) {
        return create(name, filter, null);
    }

    public static <E extends Event, V> EventNode<E> event(@NotNull String name,
                                                          @NotNull EventFilter<E, V> filter,
                                                          @NotNull Predicate<E> predicate) {
        return create(name, filter, (e, h) -> predicate.test(e));
    }

    public static <E extends Event, V> EventNode<E> value(@NotNull String name,
                                                          @NotNull EventFilter<E, V> filter,
                                                          @NotNull Predicate<V> predicate) {
        return create(name, filter, (e, h) -> predicate.test(h));
    }

    public static <E extends Event> EventNode<E> tag(@NotNull String name,
                                                     @NotNull EventFilter<E, ? extends TagReadable> filter,
                                                     @NotNull Tag<?> tag) {
        return create(name, filter, (e, h) -> h.hasTag(tag));
    }

    public static <E extends Event, V> EventNode<E> tag(@NotNull String name,
                                                        @NotNull EventFilter<E, ? extends TagReadable> filter,
                                                        @NotNull Tag<V> tag,
                                                        @NotNull Predicate<@Nullable V> consumer) {
        return create(name, filter, (e, h) -> consumer.test(h.getTag(tag)));
    }

    private static <E extends Event, V> EventNode<E> create(@NotNull String name,
                                                            @NotNull EventFilter<E, V> filter,
                                                            @Nullable BiPredicate<E, V> predicate) {
        return new EventNode<>(name, filter, predicate != null ? (e, o) -> predicate.test(e, (V) o) : null);
    }

    private static final Object GLOBAL_CHILD_LOCK = new Object();
    private final Object lock = new Object();

    private final Map<Class<? extends T>, ListenerEntry<T>> listenerMap = new ConcurrentHashMap<>();
    private final Set<EventNode<T>> children = new CopyOnWriteArraySet<>();

    protected final String name;
    protected final EventFilter<T, ?> filter;
    protected final BiPredicate<T, Object> predicate;
    protected final Class<T> eventType;
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
        return predicate.test(event, value);
    }

    public void call(@NotNull T event) {
        if (!eventType.isInstance(event)) {
            // Invalid event type
            return;
        }
        if (!condition(event)) {
            // Cancelled by superclass
            return;
        }
        // Process listener list
        final var entry = listenerMap.get(event.getClass());
        if (entry == null) {
            // No listener nor children
            return;
        }

        final var listeners = entry.listeners;
        if (listeners != null && !listeners.isEmpty()) {
            for (EventListener<T> listener : listeners) {
                final EventListener.Result result = listener.run(event);
                if (result == EventListener.Result.EXPIRED) {
                    listeners.remove(listener);
                }
            }
        }
        // Process children
        if (entry.childCount > 0) {
            for (EventNode<T> child : children) {
                child.call(event);
            }
        }
    }

    public void callCancellable(@NotNull T event, @NotNull Runnable successCallback) {
        call(event);
        if (!(event instanceof CancellableEvent) || !((CancellableEvent) event).isCancelled()) {
            successCallback.run();
        }
    }

    public @NotNull String getName() {
        return name;
    }

    public @Nullable EventNode<? super T> getParent() {
        return parent;
    }

    public @NotNull Set<@NotNull EventNode<T>> getChildren() {
        return Collections.unmodifiableSet(children);
    }

    public <E extends T> @NotNull EventNode<E> findChild(@NotNull String name, Class<E> eventType) {
        return null;
    }

    public @NotNull EventNode<T> findChild(@NotNull String name) {
        return findChild(name, eventType);
    }

    public EventNode<T> addChild(@NotNull EventNode<? extends T> child) {
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

    public EventNode<T> removeChild(@NotNull EventNode<? extends T> child) {
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

    public EventNode<T> addListener(@NotNull EventListener<? extends T> listener) {
        return addListener0(listener);
    }

    public <E extends T> EventNode<T> addListener(@NotNull Class<E> eventType, @NotNull Consumer<@NotNull E> listener) {
        return addListener0(EventListener.of(eventType, listener));
    }

    public EventNode<T> removeListener(@NotNull EventListener<? extends T> listener) {
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

    private EventNode<T> addListener0(@NotNull EventListener<? extends T> listener) {
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

    private void increaseChildListenerCount(Class<? extends T> eventClass, int count) {
        var entry = listenerMap.computeIfAbsent(eventClass, aClass -> new ListenerEntry<>());
        ListenerEntry.addAndGet(entry, count);
    }

    private void decreaseChildListenerCount(Class<? extends T> eventClass, int count) {
        var entry = listenerMap.computeIfAbsent(eventClass, aClass -> new ListenerEntry<>());
        final int result = ListenerEntry.addAndGet(entry, -count);
        if (result == 0 && entry.listeners.isEmpty()) {
            this.listenerMap.remove(eventClass);
        } else if (result < 0) {
            throw new IllegalStateException("Something wrong happened, listener count: " + result);
        }
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
}
