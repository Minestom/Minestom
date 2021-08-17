package net.minestom.server.event;

import net.minestom.server.MinecraftServer;
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
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;

class EventNodeImpl<T extends Event> implements EventNode<T> {
    private static final Object GLOBAL_CHILD_LOCK = new Object();
    private final Object lock = new Object();

    private final Map<Class<? extends T>, ListenerEntry<T>> listenerMap = new ConcurrentHashMap<>();
    private final Set<EventNode<T>> children = new CopyOnWriteArraySet<>();
    private final Map<Object, ListenerEntry<T>> mappedNodeCache = new WeakHashMap<>();

    private final String name;
    private final EventFilter<T, ?> filter;
    private final BiPredicate<T, Object> predicate;
    private final Class<T> eventType;
    private volatile int priority;
    private volatile EventNodeImpl<? super T> parent;

    protected EventNodeImpl(@NotNull String name,
                            @NotNull EventFilter<T, ?> filter,
                            @Nullable BiPredicate<T, Object> predicate) {
        this.name = name;
        this.filter = filter;
        this.predicate = predicate;
        this.eventType = filter.eventType();
    }

    @Override
    public void call(@NotNull T event) {
        final var eventClass = event.getClass();
        if (!eventType.isAssignableFrom(eventClass)) return; // Invalid event type
        // Conditions
        if (predicate != null) {
            try {
                final var value = filter.getHandler(event);
                if (!predicate.test(event, value)) return;
            } catch (Exception e) {
                MinecraftServer.getExceptionManager().handleException(e);
                return;
            }
        }
        // Process listeners list
        final var entry = listenerMap.get(eventClass);
        if (entry == null) return; // No listener nor children
        entry.call(event);
        // Process children
        if (entry.childCount > 0) {
            this.children.stream()
                    .sorted(Comparator.comparing(EventNode::getPriority))
                    .forEach(child -> child.call(event));
        }
    }

    @Override
    public <E extends T> @NotNull List<EventNode<E>> findChildren(@NotNull String name, Class<E> eventType) {
        if (children.isEmpty()) return Collections.emptyList();
        synchronized (GLOBAL_CHILD_LOCK) {
            List<EventNode<E>> result = new ArrayList<>();
            for (EventNode<T> child : children) {
                if (equals(child, name, eventType)) {
                    result.add((EventNode<E>) child);
                }
                result.addAll(child.findChildren(name, eventType));
            }
            return result;
        }
    }

    @Contract(pure = true)
    public @NotNull Set<@NotNull EventNode<T>> getChildren() {
        return Collections.unmodifiableSet(children);
    }

    @Override
    public <E extends T> void replaceChildren(@NotNull String name, @NotNull Class<E> eventType, @NotNull EventNode<E> eventNode) {
        synchronized (GLOBAL_CHILD_LOCK) {
            if (children.isEmpty()) return;
            for (EventNode<T> child : children) {
                if (equals(child, name, eventType)) {
                    removeChild(child);
                    addChild(eventNode);
                    continue;
                }
                child.replaceChildren(name, eventType, eventNode);
            }
        }
    }

    @Override
    public void removeChildren(@NotNull String name, @NotNull Class<? extends T> eventType) {
        synchronized (GLOBAL_CHILD_LOCK) {
            if (children.isEmpty()) return;
            for (EventNode<T> child : children) {
                if (equals(child, name, eventType)) {
                    removeChild(child);
                    continue;
                }
                child.removeChildren(name, eventType);
            }
        }
    }

    @Override
    public void removeChildren(@NotNull String name) {
        removeChildren(name, eventType);
    }

    @Override
    public @NotNull EventNode<T> addChild(@NotNull EventNode<? extends T> child) {
        synchronized (GLOBAL_CHILD_LOCK) {
            final var childImpl = (EventNodeImpl<? extends T>) child;
            Check.stateCondition(childImpl.parent != null, "Node already has a parent");
            Check.stateCondition(Objects.equals(parent, child), "Cannot have a child as parent");
            final boolean result = this.children.add((EventNodeImpl<T>) childImpl);
            if (result) {
                childImpl.parent = this;
                // Increase listener count
                propagateNode(childImpl, IntUnaryOperator.identity());
            }
        }
        return this;
    }

    @Override
    public @NotNull EventNode<T> removeChild(@NotNull EventNode<? extends T> child) {
        synchronized (GLOBAL_CHILD_LOCK) {
            final boolean result = this.children.remove(child);
            if (result) {
                final var childImpl = (EventNodeImpl<? extends T>) child;
                childImpl.parent = null;
                // Decrease listener count
                propagateNode(childImpl, count -> -count);
            }
        }
        return this;
    }

    @Override
    public @NotNull EventNode<T> addListener(@NotNull EventListener<? extends T> listener) {
        synchronized (GLOBAL_CHILD_LOCK) {
            final var eventType = listener.getEventType();
            var entry = getEntry(eventType);
            entry.listeners.add((EventListener<T>) listener);
            propagateToParent(eventType, 1);
        }
        return this;
    }

    @Override
    public @NotNull EventNode<T> removeListener(@NotNull EventListener<? extends T> listener) {
        synchronized (GLOBAL_CHILD_LOCK) {
            final var eventType = listener.getEventType();
            var entry = listenerMap.get(eventType);
            if (entry == null) return this;
            var listeners = entry.listeners;
            final boolean removed = listeners.remove(listener);
            if (removed) propagateToParent(eventType, -1);
        }
        return this;
    }

    @Override
    public void map(@NotNull EventNode<? extends T> node, @NotNull Object value) {
        final var nodeImpl = (EventNodeImpl<? extends T>) node;
        final var valueType = value.getClass();
        synchronized (GLOBAL_CHILD_LOCK) {
            nodeImpl.listenerMap.forEach((type, listenerEntry) -> {
                final var entry = getEntry(type);
                final boolean correct = entry.filters.stream().anyMatch(eventFilter -> {
                    final var handlerType = eventFilter.handlerType();
                    return handlerType != null && handlerType.isAssignableFrom(valueType);
                });
                Check.stateCondition(!correct, "The node filter {0} is not compatible with type {1}", nodeImpl.eventType, valueType);
                synchronized (mappedNodeCache) {
                    entry.mappedNode.put(value, (EventNode<T>) nodeImpl);
                    mappedNodeCache.put(value, entry);
                    // TODO propagate
                }
            });
        }
    }

    @Override
    public boolean unmap(@NotNull Object value) {
        synchronized (GLOBAL_CHILD_LOCK) {
            synchronized (mappedNodeCache) {
                var entry = mappedNodeCache.remove(value);
                if (entry == null) return false;
                final EventNode<T> previousNode = entry.mappedNode.remove(value);
                if (previousNode != null) {
                    // TODO propagate
                    return true;
                }
                return false;
            }
        }
    }

    @Override
    public void register(@NotNull EventBinding<? extends T> binding) {
        synchronized (GLOBAL_CHILD_LOCK) {
            for (var eventType : binding.eventTypes()) {
                var entry = getEntry((Class<? extends T>) eventType);
                final boolean added = entry.bindingConsumers.add((Consumer<T>) binding.consumer(eventType));
                if (added) propagateToParent((Class<? extends T>) eventType, 1);
            }
        }
    }

    @Override
    public void unregister(@NotNull EventBinding<? extends T> binding) {
        synchronized (GLOBAL_CHILD_LOCK) {
            for (var eventType : binding.eventTypes()) {
                var entry = listenerMap.get(eventType);
                if (entry == null) return;
                final boolean removed = entry.bindingConsumers.remove(binding.consumer(eventType));
                if (removed) propagateToParent((Class<? extends T>) eventType, -1);
            }
        }
    }

    @Override
    public @NotNull Class<T> getEventType() {
        return eventType;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public @NotNull EventNode<T> setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    @Override
    public @Nullable EventNode<? super T> getParent() {
        return parent;
    }

    private void propagateChildCountChange(Class<? extends T> eventClass, int count) {
        var entry = getEntry(eventClass);
        final int result = ListenerEntry.CHILD_UPDATER.addAndGet(entry, count);
        if (result == 0 && entry.listeners.isEmpty()) {
            this.listenerMap.remove(eventClass);
        } else if (result < 0) {
            throw new IllegalStateException("Something wrong happened, listener count: " + result);
        }
        if (parent != null) {
            parent.propagateChildCountChange(eventClass, count);
        }
    }

    private void propagateToParent(Class<? extends T> eventClass, int count) {
        final var parent = this.parent;
        if (parent != null) {
            synchronized (parent.lock) {
                parent.propagateChildCountChange(eventClass, count);
            }
        }
    }

    private void propagateNode(EventNodeImpl<? extends T> child, IntUnaryOperator operator) {
        synchronized (lock) {
            final var listeners = child.listenerMap;
            listeners.forEach((eventClass, eventListeners) -> {
                final var entry = listeners.get(eventClass);
                if (entry == null) return;
                final int childCount = entry.listeners.size() + entry.childCount;
                propagateChildCountChange(eventClass, operator.applyAsInt(childCount));
            });
        }
    }

    private ListenerEntry<T> getEntry(Class<? extends T> type) {
        return listenerMap.computeIfAbsent(type, aClass -> new ListenerEntry<>(this, (Class<T>) aClass));
    }

    private static boolean equals(EventNode<?> node, String name, Class<?> eventType) {
        final boolean nameCheck = node.getName().equals(name);
        final boolean typeCheck = eventType.isAssignableFrom(((EventNodeImpl<?>) node).eventType);
        return nameCheck && typeCheck;
    }

    private static class ListenerEntry<T extends Event> {
        private static final List<EventFilter<? extends Event, ?>> FILTERS = List.of(
                EventFilter.ENTITY,
                EventFilter.ITEM, EventFilter.INSTANCE,
                EventFilter.INVENTORY, EventFilter.BLOCK);
        @SuppressWarnings("rawtypes")
        private static final AtomicIntegerFieldUpdater<ListenerEntry> CHILD_UPDATER =
                AtomicIntegerFieldUpdater.newUpdater(ListenerEntry.class, "childCount");

        final EventNodeImpl<T> node;
        final List<EventFilter<?, ?>> filters;
        final List<EventListener<T>> listeners = new CopyOnWriteArrayList<>();
        final Set<Consumer<T>> bindingConsumers = new CopyOnWriteArraySet<>();
        final Map<Object, EventNode<T>> mappedNode = new WeakHashMap<>();
        volatile int childCount;

        ListenerEntry(EventNodeImpl<T> node, Class<T> eventType) {
            this.node = node;
            this.filters = FILTERS.stream().filter(eventFilter -> eventFilter.eventType().isAssignableFrom(eventType)).collect(Collectors.toList());
        }

        void call(T event) {
            // Event interfaces
            if (!bindingConsumers.isEmpty()) {
                for (var consumer : bindingConsumers) {
                    consumer.accept(event);
                }
            }
            // Mapped listeners
            if (!mappedNode.isEmpty()) {
                synchronized (node.mappedNodeCache) {
                    // Check mapped listeners for each individual event handler
                    for (var filter : filters) {
                        final var handler = filter.castHandler(event);
                        final var map = mappedNode.get(handler);
                        if (map != null) map.call(event);
                    }
                }
            }
            // Basic listeners
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
        }
    }
}
