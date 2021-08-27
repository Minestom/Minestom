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
    public <E extends T> @NotNull ListenerHandle<E> getHandle(@NotNull Class<E> handleType) {
        //noinspection unchecked
        return (ListenerHandle<E>) handleMap.computeIfAbsent(handleType,
                aClass -> new Handle<>(this, (Class<T>) aClass));
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
    public @NotNull EventNode<T> addChild(@NotNull EventNode<? extends T> child) {
        synchronized (GLOBAL_CHILD_LOCK) {
            final var childImpl = (EventNodeImpl<? extends T>) child;
            Check.stateCondition(childImpl.parent != null, "Node already has a parent");
            Check.stateCondition(Objects.equals(parent, child), "Cannot have a child as parent");
            if (!children.add((EventNodeImpl<T>) childImpl)) return this; // Couldn't add the child (already present?)
            childImpl.parent = this;
            childImpl.invalidateEventsFor(this);
        }
        return this;
    }

    @Override
    public @NotNull EventNode<T> removeChild(@NotNull EventNode<? extends T> child) {
        synchronized (GLOBAL_CHILD_LOCK) {
            final var childImpl = (EventNodeImpl<? extends T>) child;
            final boolean result = this.children.remove(childImpl);
            if (!result) return this; // Child not found
            childImpl.parent = null;
            childImpl.invalidateEventsFor(this);
        }
        return this;
    }

    @Override
    public @NotNull EventNode<T> addListener(@NotNull EventListener<? extends T> listener) {
        synchronized (GLOBAL_CHILD_LOCK) {
            final var eventType = listener.eventType();
            ListenerEntry<T> entry = getEntry(eventType);
            entry.listeners.add((EventListener<T>) listener);
            invalidateEvent(eventType);
        }
        return this;
    }

    @Override
    public @NotNull EventNode<T> removeListener(@NotNull EventListener<? extends T> listener) {
        synchronized (GLOBAL_CHILD_LOCK) {
            final var eventType = listener.eventType();
            ListenerEntry<T> entry = listenerMap.get(eventType);
            if (entry == null) return this; // There is no listener with such type
            if (entry.listeners.remove(listener)) invalidateEvent(eventType);
        }
        return this;
    }

    @Override
    public void map(@NotNull EventNode<? extends T> node, @NotNull Object value) {
        synchronized (GLOBAL_CHILD_LOCK) {
            final var nodeImpl = (EventNodeImpl<? extends T>) node;
            Check.stateCondition(nodeImpl.parent != null, "Node already has a parent");
            Check.stateCondition(Objects.equals(parent, nodeImpl), "Cannot map to self");
            EventNodeImpl<T> previous = this.mappedNodeCache.put(value, (EventNodeImpl<T>) nodeImpl);
            if (previous != null) previous.parent = null;
            nodeImpl.parent = this;
            nodeImpl.invalidateEventsFor(this);
        }
    }

    @Override
    public boolean unmap(@NotNull Object value) {
        synchronized (GLOBAL_CHILD_LOCK) {
            final var mappedNode = this.mappedNodeCache.remove(value);
            if (mappedNode == null) return false; // Mapped node not found
            final var childImpl = (EventNodeImpl<? extends T>) mappedNode;
            childImpl.parent = null;
            childImpl.invalidateEventsFor(this);
            return true;
        }
    }

    @Override
    public void register(@NotNull EventBinding<? extends T> binding) {
        synchronized (GLOBAL_CHILD_LOCK) {
            for (var eventType : binding.eventTypes()) {
                ListenerEntry<T> entry = getEntry((Class<? extends T>) eventType);
                final boolean added = entry.bindingConsumers.add((Consumer<T>) binding.consumer(eventType));
                if (added) invalidateEvent((Class<? extends T>) eventType);
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
                if (removed) invalidateEvent((Class<? extends T>) eventType);
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

    private void invalidateEventsFor(EventNodeImpl<? super T> node) {
        for (Class<? extends T> eventType : listenerMap.keySet()) {
            node.invalidateEvent(eventType);
        }
        // TODO bindings?
        for (EventNodeImpl<T> child : children) {
            child.invalidateEventsFor(node);
        }
    }

    private void invalidateEvent(Class<? extends T> eventClass) {
        forTargetEvents(eventClass, type -> {
            Handle<? super T> handle = handleMap.get(type);
            if (handle != null) handle.updated = false;
        });
        final EventNodeImpl<? super T> parent = this.parent;
        if (parent != null) parent.invalidateEvent(eventClass);
    }

    private ListenerEntry<T> getEntry(Class<? extends T> type) {
        return listenerMap.computeIfAbsent(type, aClass -> new ListenerEntry<>());
    }

    private static boolean equals(EventNode<?> node, String name, Class<?> eventType) {
        return node.getName().equals(name) && eventType.isAssignableFrom((node.getEventType()));
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
        private Consumer<E>[] listeners = new Consumer[0];
        private final List<Consumer<E>> listenersCache = new ArrayList<>();
        private volatile boolean updated;

        Handle(EventNodeImpl<E> node, Class<E> eventType) {
            this.node = node;
            this.eventType = eventType;
        }

        @Override
        public void call(@NotNull E event) {
            update();
            final Consumer<E>[] listeners = this.listeners;
            if (listeners.length == 0) return;
            for (Consumer<E> listener : listeners) {
                listener.accept(event);
            }
        }

        @Override
        public boolean hasListener() {
            update();
            return listeners.length > 0;
        }

        void update() {
            if (updated) return;
            synchronized (GLOBAL_CHILD_LOCK) {
                if (updated) return;
                this.listenersCache.clear();
                recursiveUpdate(node);
                this.listeners = listenersCache.toArray(Consumer[]::new);
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
            final Set<EventNodeImpl<E>> children = targetNode.children;
            if (children.isEmpty()) return;
            children.stream()
                    .filter(child -> child.eventType.isAssignableFrom(eventType)) // Invalid event type
                    .sorted(Comparator.comparing(EventNode::getPriority))
                    .forEach(this::recursiveUpdate);
        }

        /**
         * Add the node's listeners from {@link EventNode#map(EventNode, Object)}.
         * The goal is to limit the amount of map lookup.
         */
        private void handleMappedNode(EventNodeImpl<E> targetNode) {
            final var mappedNodeCache = targetNode.mappedNodeCache;
            if (mappedNodeCache.isEmpty()) return;
            Set<EventFilter<E, ?>> filters = new HashSet<>(mappedNodeCache.size());
            Map<Object, Handle<E>> handlers = new HashMap<>(mappedNodeCache.size());
            // Retrieve all filters used to retrieve potential handlers
            for (var mappedEntry : mappedNodeCache.entrySet()) {
                final EventNodeImpl<E> mappedNode = mappedEntry.getValue();
                final Handle<E> handle = (Handle<E>) mappedNode.getHandle(eventType);
                if (!handle.hasListener()) continue; // Implicit update
                filters.add(mappedNode.filter);
                handlers.put(mappedEntry.getKey(), handle);
            }
            // If at least one mapped node listen to this handle type,
            // loop through them and forward to mapped node if there is a match
            if (!filters.isEmpty()) {
                final EventFilter<E, ?>[] filterList = filters.toArray(EventFilter[]::new);
                final BiConsumer<EventFilter<E, ?>, E> mapper = (filter, event) -> {
                    final Object handler = filter.castHandler(event);
                    final Handle<E> handle = handlers.get(handler);
                    if (handle != null) handle.call(event);
                };
                if (filterList.length == 1) {
                    final var firstFilter = filterList[0];
                    // Common case where there is only one filter
                    this.listenersCache.add(event -> mapper.accept(firstFilter, event));
                } else if (filterList.length == 2) {
                    final var firstFilter = filterList[0];
                    final var secondFilter = filterList[1];
                    this.listenersCache.add(event -> {
                        mapper.accept(firstFilter, event);
                        mapper.accept(secondFilter, event);
                    });
                } else {
                    this.listenersCache.add(event -> {
                        for (var filter : filterList) {
                            mapper.accept(filter, event);
                        }
                    });
                }
            }
        }

        /**
         * Add listeners from {@link EventNode#addListener(EventListener)} and
         * {@link EventNode#register(EventBinding)} to the handle list.
         * <p>
         * Most computation should ideally be done outside the consumers as a one-time cost.
         */
        private void appendEntries(ListenerEntry<E> entry, EventNodeImpl<E> targetNode) {
            final var filter = targetNode.filter;
            final var predicate = targetNode.predicate;

            final boolean hasPredicate = predicate != null;
            final EventListener<E>[] listenersCopy = entry.listeners.toArray(EventListener[]::new);
            final Consumer<E>[] bindingsCopy = entry.bindingConsumers.toArray(Consumer[]::new);
            final boolean listenersEmpty = listenersCopy.length == 0;
            final boolean bindingsEmpty = bindingsCopy.length == 0;
            if (!hasPredicate && listenersEmpty && bindingsEmpty)
                return; // Nothing to run

            if (!hasPredicate && bindingsEmpty && listenersCopy.length == 1) {
                // Only one normal listener
                final EventListener<E> listener = listenersCopy[0];
                this.listenersCache.add(e -> callListener(targetNode, listener, e));
                return;
            }
            // Worse case scenario, try to run everything
            this.listenersCache.add(e -> {
                if (hasPredicate) {
                    final Object value = filter.getHandler(e);
                    try {
                        if (!predicate.test(e, value)) return;
                    } catch (Throwable t) {
                        MinecraftServer.getExceptionManager().handleException(t);
                        return;
                    }
                }
                if (!listenersEmpty) {
                    for (EventListener<E> listener : listenersCopy) {
                        callListener(targetNode, listener, e);
                    }
                }
                if (!bindingsEmpty) {
                    for (Consumer<E> eConsumer : bindingsCopy) {
                        try {
                            eConsumer.accept(e);
                        } catch (Throwable t) {
                            MinecraftServer.getExceptionManager().handleException(t);
                        }
                    }
                }
            });
        }

        static <E extends Event> void callListener(EventNodeImpl<E> targetNode, EventListener<E> listener, E event) {
            EventListener.Result result;
            try {
                result = listener.run(event);
            } catch (Throwable t) {
                result = EventListener.Result.EXCEPTION;
                MinecraftServer.getExceptionManager().handleException(t);
            }
            if (result == EventListener.Result.EXPIRED) {
                targetNode.removeListener(listener);
            }
        }
    }
}
