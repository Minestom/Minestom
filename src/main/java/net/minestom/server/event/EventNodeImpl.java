package net.minestom.server.event;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.trait.RecursiveEvent;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

class EventNodeImpl<T extends Event> implements EventNode<T> {
    private static final Object GLOBAL_CHILD_LOCK = new Object();

    private final Map<Class<? extends T>, Handle<T>> handleMap = new ConcurrentHashMap<>();
    private final Map<Class<? extends T>, ListenerEntry<T>> listenerMap = new ConcurrentHashMap<>();
    private final Set<EventNodeImpl<T>> children = new CopyOnWriteArraySet<>();
    private final Map<Object, EventNodeImpl<T>> mappedNodeCache = new WeakHashMap<>();

    private final String name;
    private final EventFilter<T, ?> filter;
    private final BiPredicate<T, Object> predicate;
    private final Class<T> eventType;
    private volatile int priority;
    private volatile EventNodeImpl<? super T> parent;

    EventNodeImpl(@NotNull String name,
                  @NotNull EventFilter<T, ?> filter,
                  @Nullable BiPredicate<T, Object> predicate) {
        this.name = name;
        this.filter = filter;
        this.predicate = predicate;
        this.eventType = filter.eventType();
    }

    @Override
    public <E extends T> void call(@NotNull E event, @NotNull ListenerHandle<E> handle) {
        final Handle<T> castedHandle = (Handle<T>) handle;
        Check.argCondition(castedHandle.node != this, "Invalid handle owner");
        if (!castedHandle.updated) castedHandle.update();
        final List<Consumer<T>> listeners = castedHandle.listeners;
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
    public boolean hasListener(@NotNull ListenerHandle<? extends T> handle) {
        final Handle<T> castedHandle = (Handle<T>) handle;
        if (!castedHandle.updated) castedHandle.update();
        return !castedHandle.listeners.isEmpty();
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
            if (!children.add((EventNodeImpl<T>) childImpl)) return this; // Couldn't add the child (already present?)
            childImpl.parent = this;
            childImpl.propagateEvents(this); // Propagate after setting the parent
        }
        return this;
    }

    @Override
    public @NotNull EventNode<T> removeChild(@NotNull EventNode<? extends T> child) {
        synchronized (GLOBAL_CHILD_LOCK) {
            final var childImpl = (EventNodeImpl<? extends T>) child;
            final boolean result = this.children.remove(childImpl);
            if (!result) return this; // Child not found
            childImpl.propagateEvents(parent); // Propagate before removing the parent
            childImpl.parent = null;
        }
        return this;
    }

    @Override
    public @NotNull EventNode<T> addListener(@NotNull EventListener<? extends T> listener) {
        synchronized (GLOBAL_CHILD_LOCK) {
            final var eventType = listener.eventType();
            ListenerEntry<T> entry = getEntry(eventType);
            entry.listeners.add((EventListener<T>) listener);
            propagateEvent(parent, eventType);
        }
        return this;
    }

    @Override
    public @NotNull EventNode<T> removeListener(@NotNull EventListener<? extends T> listener) {
        synchronized (GLOBAL_CHILD_LOCK) {
            final var eventType = listener.eventType();
            ListenerEntry<T> entry = listenerMap.get(eventType);
            if (entry == null) return this; // There is no listener with such type
            var listeners = entry.listeners;
            if (listeners.remove(listener)) propagateEvent(parent, eventType);
        }
        return this;
    }

    @Override
    public void map(@NotNull EventNode<? extends T> node, @NotNull Object value) {
        synchronized (GLOBAL_CHILD_LOCK) {
            final var nodeImpl = (EventNodeImpl<? extends T>) node;
            Check.stateCondition(nodeImpl.parent != null, "Node already has a parent");
            Check.stateCondition(Objects.equals(parent, nodeImpl), "Cannot map to self");
            var previous = this.mappedNodeCache.put(value, (EventNodeImpl<T>) nodeImpl);
            if (previous != null) previous.parent = null;
            nodeImpl.parent = this;
            nodeImpl.propagateEvents(this); // Propagate after setting the parent
        }
    }

    @Override
    public boolean unmap(@NotNull Object value) {
        synchronized (GLOBAL_CHILD_LOCK) {
            final var mappedNode = this.mappedNodeCache.remove(value);
            if (mappedNode == null) return false; // Mapped node not found
            final var childImpl = (EventNodeImpl<? extends T>) mappedNode;
            childImpl.propagateEvents(parent); // Propagate before removing the parent
            childImpl.parent = null;
            return true;
        }
    }

    @Override
    public void register(@NotNull EventBinding<? extends T> binding) {
        synchronized (GLOBAL_CHILD_LOCK) {
            for (var eventType : binding.eventTypes()) {
                ListenerEntry<T> entry = getEntry((Class<? extends T>) eventType);
                final boolean added = entry.bindingConsumers.add((Consumer<T>) binding.consumer(eventType));
                if (added) propagateEvent(parent, (Class<? extends T>) eventType);
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
                if (removed) propagateEvent(parent, (Class<? extends T>) eventType);
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

    private void propagateEvents(EventNodeImpl<? super T> parent) {
        this.listenerMap.keySet().forEach(aClass -> propagateEvent(parent, aClass));
    }

    private void propagateEvent(EventNodeImpl parent, Class<? extends T> eventClass) {
        if (parent == null) return;
        forTargetEvents(eventClass, type -> {
            Handle<? super T> parentHandle = (Handle<? super T>) parent.handleMap.get(type);
            if (parentHandle == null) return;
            parentHandle.updated = false;
            parent.propagateEvent(parent.parent, type);
        });
    }

    private ListenerEntry<T> getEntry(Class<? extends T> type) {
        return listenerMap.computeIfAbsent(type, aClass -> new ListenerEntry<>());
    }

    private static boolean equals(EventNode<?> node, String name, Class<?> eventType) {
        final boolean nameCheck = node.getName().equals(name);
        final boolean typeCheck = eventType.isAssignableFrom(((EventNodeImpl<?>) node).eventType);
        return nameCheck && typeCheck;
    }

    private static void forTargetEvents(Class<?> type, Consumer<Class<?>> consumer) {
        consumer.accept(type);
        // Recursion
        if (RecursiveEvent.class.isAssignableFrom(type)) {
            final Class<?> superclass = type.getSuperclass();
            if (superclass != null && RecursiveEvent.class.isAssignableFrom(superclass)) {
                forTargetEvents(superclass, consumer);
            }
        }
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
                this.listeners.clear();
                recursiveUpdate(node);
                this.updated = true;
            }
        }

        private void recursiveUpdate(EventNodeImpl<E> targetNode) {
            // Standalone listeners
            forTargetEvents(eventType, type -> {
                final ListenerEntry<E> entry = targetNode.listenerMap.get(type);
                if (entry != null) appendEntries(entry, targetNode);
            });
            // Mapped nodes
            handleMappedNode(targetNode);
            // Add children
            final var children = targetNode.children;
            if (children.isEmpty()) return;
            children.stream()
                    .filter(child -> child.eventType.isAssignableFrom(eventType)) // Invalid event type
                    .sorted(Comparator.comparing(EventNode::getPriority))
                    .forEach(this::recursiveUpdate);
        }

        private void handleMappedNode(EventNodeImpl<E> targetNode) {
            final var mappedNodeCache = targetNode.mappedNodeCache;
            if (mappedNodeCache.isEmpty()) return;
            Set<EventFilter<E, ?>> filters = new HashSet<>(mappedNodeCache.size());
            Map<Object, Handle<E>> handlers = new HashMap<>(mappedNodeCache.size());
            // Retrieve all filters used to retrieve potential handlers
            for (var mappedEntry : mappedNodeCache.entrySet()) {
                final EventNodeImpl<E> mappedNode = mappedEntry.getValue();
                final var handle = (Handle<E>) mappedNode.getHandle(eventType);
                if (!mappedNode.hasListener(handle)) continue; // Implicit update
                filters.add(mappedNode.filter);
                handlers.put(mappedEntry.getKey(), handle);
            }
            // If at least one mapped node listen to this handle type,
            // loop through them and forward to mapped node if there is a match
            if (!filters.isEmpty()) {
                final var filterList = List.copyOf(filters);
                final int size = filterList.size();
                final BiConsumer<EventFilter<E, ?>, E> mapper = (filter, event) -> {
                    final Object handler = filter.castHandler(event);
                    final var handle = handlers.get(handler);
                    if (handle != null) {
                        if (!handle.updated) handle.update();
                        for (Consumer<E> listener : handle.listeners) {
                            listener.accept(event);
                        }
                    }
                };
                if (size == 1) {
                    final var firstFilter = filterList.get(0);
                    // Common case where there is only one filter
                    this.listeners.add(event -> mapper.accept(firstFilter, event));
                } else if (size == 2) {
                    final var firstFilter = filterList.get(0);
                    final var secondFilter = filterList.get(1);
                    this.listeners.add(event -> {
                        mapper.accept(firstFilter, event);
                        mapper.accept(secondFilter, event);
                    });
                } else {
                    this.listeners.add(event -> {
                        for (var filter : filterList) {
                            mapper.accept(filter, event);
                        }
                    });
                }
            }
        }

        private void appendEntries(ListenerEntry<E> entry, EventNodeImpl<E> targetNode) {
            final var filter = targetNode.filter;
            final var predicate = targetNode.predicate;

            final boolean hasPredicate = predicate != null;
            final var listenersCopy = List.copyOf(entry.listeners);
            final var bindingsCopy = List.copyOf(entry.bindingConsumers);
            if (!hasPredicate && listenersCopy.isEmpty() && bindingsCopy.isEmpty())
                return; // Nothing to run

            if (!hasPredicate && bindingsCopy.isEmpty() && listenersCopy.size() == 1) {
                // Only one normal listener
                final EventListener<E> listener = listenersCopy.get(0);
                this.listeners.add(e -> callListener(targetNode, listener, e));
                return;
            }

            // Worse case scenario, try to run everything
            this.listeners.add(e -> {
                if (hasPredicate) {
                    final var value = filter.getHandler(e);
                    if (!predicate.test(e, value)) return;
                }
                if (!listenersCopy.isEmpty()) {
                    for (EventListener<E> listener : listenersCopy) {
                        callListener(targetNode, listener, e);
                    }
                }
                if (!bindingsCopy.isEmpty()) {
                    for (Consumer<E> eConsumer : bindingsCopy) {
                        eConsumer.accept(e);
                    }
                }
            });
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
