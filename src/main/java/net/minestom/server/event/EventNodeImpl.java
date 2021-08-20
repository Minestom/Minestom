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
import java.util.function.BiPredicate;
import java.util.function.Consumer;

class EventNodeImpl<T extends Event> implements EventNode<T> {
    private static final Object GLOBAL_CHILD_LOCK = new Object();

    private final Map<Class<? extends T>, Handle<T>> handleMap = new ConcurrentHashMap<>();
    private final Map<Class<? extends T>, ListenerEntry<T>> listenerMap = new ConcurrentHashMap<>();
    private final Set<EventNodeImpl<T>> children = new CopyOnWriteArraySet<>();
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
    public <E extends T> void call(@NotNull E event, @NotNull ListenerHandle<E> handle) {
        var castedHandle = (Handle<T>) handle;
        Check.argCondition(castedHandle.node != this, "Invalid handle owner");
        List<Consumer<T>> listeners = castedHandle.listeners;
        if (!castedHandle.updated) {
            castedHandle.update();
        }
        if (listeners.isEmpty()) return;
        for (Consumer<T> listener : listeners) {
            listener.accept(event);
        }
    }

    @Override
    public <E extends T> @NotNull ListenerHandle<E> getHandle(@NotNull Class<E> handleType) {
        //noinspection unchecked
        return (ListenerHandle<E>) handleMap.computeIfAbsent(handleType,
                aClass -> new Handle<>(this, (Class<T>) aClass));
    }

    @Override
    public <E extends T> boolean hasListener(@NotNull ListenerHandle<E> handle) {
        var castedHandle = (Handle<T>) handle;
        List<Consumer<T>> listeners = castedHandle.listeners;
        if (!castedHandle.updated) {
            castedHandle.update();
        }
        return !listeners.isEmpty();
    }

    @Override
    public <E extends T> @NotNull List<EventNode<E>> findChildren(@NotNull String name, Class<E> eventType) {
        synchronized (GLOBAL_CHILD_LOCK) {
            if (children.isEmpty()) return Collections.emptyList();
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
            if (!result) return this;
            childImpl.parent = this;
            childImpl.propagateEvents(); // Propagate after setting the parent
        }
        return this;
    }

    @Override
    public @NotNull EventNode<T> removeChild(@NotNull EventNode<? extends T> child) {
        synchronized (GLOBAL_CHILD_LOCK) {
            final boolean result = this.children.remove(child);
            if (!result) return this;
            final var childImpl = (EventNodeImpl<? extends T>) child;
            childImpl.propagateEvents(); // Propagate before removing the parent
            childImpl.parent = null;
        }
        return this;
    }

    @Override
    public @NotNull EventNode<T> addListener(@NotNull EventListener<? extends T> listener) {
        synchronized (GLOBAL_CHILD_LOCK) {
            final var eventType = listener.eventType();
            var entry = getEntry(eventType);
            entry.listeners.add((EventListener<T>) listener);
            propagateEvent(eventType);
        }
        return this;
    }

    @Override
    public @NotNull EventNode<T> removeListener(@NotNull EventListener<? extends T> listener) {
        synchronized (GLOBAL_CHILD_LOCK) {
            final var eventType = listener.eventType();
            var entry = listenerMap.get(eventType);
            if (entry == null) return this;
            var listeners = entry.listeners;
            final boolean removed = listeners.remove(listener);
            if (removed) propagateEvent(eventType);
        }
        return this;
    }

    @Override
    public void map(@NotNull EventNode<? extends T> node, @NotNull Object value) {
        // TODO
    }

    @Override
    public boolean unmap(@NotNull Object value) {
        // TODO
        return false;
    }

    @Override
    public void register(@NotNull EventBinding<? extends T> binding) {
        synchronized (GLOBAL_CHILD_LOCK) {
            for (var eventType : binding.eventTypes()) {
                ListenerEntry<T> entry = getEntry((Class<? extends T>) eventType);
                final boolean added = entry.bindingConsumers.add((Consumer<T>) binding.consumer(eventType));
                if (added) propagateEvent((Class<? extends T>) eventType);
            }
        }
    }

    @Override
    public void unregister(@NotNull EventBinding<? extends T> binding) {
        synchronized (GLOBAL_CHILD_LOCK) {
            for (var eventType : binding.eventTypes()) {
                ListenerEntry<T> entry = listenerMap.get(eventType);
                if (entry == null) return;
                final boolean removed = entry.bindingConsumers.remove(binding.consumer(eventType));
                if (removed) propagateEvent((Class<? extends T>) eventType);
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

    private void propagateEvents() {
        this.listenerMap.keySet().forEach(this::propagateEvent);
    }

    private void propagateEvent(Class<? extends T> eventClass) {
        final var parent = this.parent;
        if (parent == null) return;
        Handle<? super T> parentHandle = parent.handleMap.get(eventClass);
        if (parentHandle == null) return;
        parentHandle.updated = false;
        parent.propagateEvent(eventClass);
    }

    private ListenerEntry<T> getEntry(Class<? extends T> type) {
        return listenerMap.computeIfAbsent(type, aClass -> new ListenerEntry<>());
    }

    private static boolean equals(EventNode<?> node, String name, Class<?> eventType) {
        final boolean nameCheck = node.getName().equals(name);
        final boolean typeCheck = eventType.isAssignableFrom(((EventNodeImpl<?>) node).eventType);
        return nameCheck && typeCheck;
    }

    private static class ListenerEntry<T extends Event> {
        final List<EventListener<T>> listeners = new CopyOnWriteArrayList<>();
        final Set<Consumer<T>> bindingConsumers = new CopyOnWriteArraySet<>();
    }

    private static final class Handle<E extends Event> implements ListenerHandle<E> {
        private final EventNodeImpl<E> node;
        private final Class<E> eventType;
        private final List<Consumer<E>> listeners = new CopyOnWriteArrayList<>();
        private volatile boolean updated;

        Handle(EventNodeImpl<E> node, Class<E> eventType) {
            this.node = node;
            this.eventType = eventType;
        }

        void update() {
            synchronized (GLOBAL_CHILD_LOCK) {
                listeners.clear();
                recursiveUpdate(node);
                updated = true;
            }
        }

        private void recursiveUpdate(EventNodeImpl<E> targetNode) {
            final var handleType = eventType;
            ListenerEntry<E> entry = targetNode.listenerMap.get(handleType);
            if (entry != null) appendEntry(listeners, entry, targetNode);
            // Add children
            final var children = targetNode.children;
            if (children.isEmpty()) return;
            children.stream()
                    .filter(child -> child.eventType.isAssignableFrom(handleType)) // Invalid event type
                    .sorted(Comparator.comparing(EventNode::getPriority))
                    .forEach(this::recursiveUpdate);
        }

        static <E extends Event> void appendEntry(List<Consumer<E>> handleListeners, ListenerEntry<E> entry, EventNodeImpl<E> targetNode) {
            final var filter = targetNode.filter;
            final var predicate = targetNode.predicate;
            // Add normal listeners
            for (var listener : entry.listeners) {
                if (predicate != null) {
                    // Ensure that the event is valid before running
                    handleListeners.add(e -> {
                        final var value = filter.getHandler(e);
                        if (!predicate.test(e, value)) return;
                        callListener(targetNode, listener, e);
                    });
                } else {
                    // No predicate, run directly
                    handleListeners.add(e -> callListener(targetNode, listener, e));
                }
            }
            // Add bindings
            handleListeners.addAll(entry.bindingConsumers);
            // TODO mapped node
        }

        static <E extends Event> void callListener(EventNodeImpl<E> targetNode, EventListener<E> listener, E event) {
            EventListener.Result result;
            try {
                result = listener.run(event);
            } catch (Exception e) {
                result = EventListener.Result.EXCEPTION;
                MinecraftServer.getExceptionManager().handleException(e);
            }
            if (result == EventListener.Result.EXPIRED) {
                targetNode.removeListener(listener);
            }
        }
    }
}
