package net.minestom.server.event;

import com.github.benmanes.caffeine.cache.Caffeine;
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

non-sealed class EventNodeImpl<T extends Event> implements EventNode<T> {
    static final Object GLOBAL_CHILD_LOCK = new Object();

    private final Map<Class, Handle<T>> handleMap = new ConcurrentHashMap<>();
    final Map<Class<? extends T>, ListenerEntry<T>> listenerMap = new ConcurrentHashMap<>();
    final Set<EventNodeImpl<T>> children = new CopyOnWriteArraySet<>();
    final Map<Object, EventNodeImpl<T>> mappedNodeCache = Caffeine.newBuilder()
            .weakKeys().weakValues().<Object, EventNodeImpl<T>>build().asMap();
    final Map<Object, EventNodeImpl<T>> registeredMappedNode = Caffeine.newBuilder()
            .weakKeys().weakValues().<Object, EventNodeImpl<T>>build().asMap();

    final String name;
    final EventFilter<T, ?> filter;
    final BiPredicate<T, Object> predicate;
    final Class<T> eventType;
    volatile int priority;
    volatile EventNodeImpl<? super T> parent;

    EventNodeImpl(@NotNull String name,
                  @NotNull EventFilter<T, ?> filter,
                  @Nullable BiPredicate<T, Object> predicate) {
        this.name = name;
        this.filter = filter;
        this.predicate = predicate;
        this.eventType = filter.eventType();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends T> @NotNull ListenerHandle<E> getHandle(@NotNull Class<E> handleType) {
        return (ListenerHandle<E>) handleMap.computeIfAbsent(handleType,
                aClass -> new Handle<>((Class<T>) aClass));
    }

    @Override
    public <E extends T> @NotNull List<EventNode<E>> findChildren(@NotNull String name, Class<E> eventType) {
        synchronized (GLOBAL_CHILD_LOCK) {
            final Set<EventNode<T>> children = getChildren();
            if (children.isEmpty()) return List.of();
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
            final Set<EventNode<T>> children = getChildren();
            if (children.isEmpty()) return;
            for (EventNode<T> child : children) {
                if (equals(child, name, eventType)) {
                    removeChild(child);
                    addChild(eventNode);
                    break;
                }
                child.replaceChildren(name, eventType, eventNode);
            }
        }
    }

    @Override
    public void removeChildren(@NotNull String name, @NotNull Class<? extends T> eventType) {
        synchronized (GLOBAL_CHILD_LOCK) {
            final Set<EventNode<T>> children = getChildren();
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
    public @NotNull <E extends T, H> EventNode<E> map(@NotNull H value, @NotNull EventFilter<E, H> filter) {
        EventNodeImpl<E> node;
        synchronized (GLOBAL_CHILD_LOCK) {
            node = new EventNodeLazyImpl<>(this, value, filter);
            Check.stateCondition(node.parent != null, "Node already has a parent");
            Check.stateCondition(Objects.equals(parent, node), "Cannot map to self");
            EventNodeImpl<T> previous = this.mappedNodeCache.putIfAbsent(value, (EventNodeImpl<T>) node);
            if (previous != null) return (EventNode<E>) previous;
            node.parent = this;
        }
        return node;
    }

    @Override
    public void unmap(@NotNull Object value) {
        synchronized (GLOBAL_CHILD_LOCK) {
            final var mappedNode = this.registeredMappedNode.remove(value);
            if (mappedNode != null) mappedNode.invalidateEventsFor(this);
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

    @Override
    public String toString() {
        return createStringGraph(createGraph());
    }

    Graph createGraph() {
        synchronized (GLOBAL_CHILD_LOCK) {
            List<Graph> children = this.children.stream().map(EventNodeImpl::createGraph).toList();
            return new Graph(getName(), getEventType().getSimpleName(), getPriority(), children);
        }
    }

    static String createStringGraph(Graph graph) {
        StringBuilder buffer = new StringBuilder();
        genToStringTree(buffer, "", "", graph);
        return buffer.toString();
    }

    private static void genToStringTree(StringBuilder buffer, String prefix, String childrenPrefix, Graph graph) {
        buffer.append(prefix);
        buffer.append(String.format("%s - EventType: %s - Priority: %d", graph.name(), graph.eventType(), graph.priority()));
        buffer.append('\n');
        var nextNodes = graph.children();
        for (Iterator<? extends @NotNull Graph> iterator = nextNodes.iterator(); iterator.hasNext(); ) {
            Graph next = iterator.next();
            if (iterator.hasNext()) {
                genToStringTree(buffer, childrenPrefix + '├' + '─' + " ", childrenPrefix + '│' + "   ", next);
            } else {
                genToStringTree(buffer, childrenPrefix + '└' + '─' + " ", childrenPrefix + "    ", next);
            }
        }
    }

    record Graph(String name, String eventType, int priority,
                 List<Graph> children) {
        public Graph {
            children = children.stream().sorted(Comparator.comparingInt(Graph::priority)).toList();
        }
    }

    void invalidateEventsFor(EventNodeImpl<? super T> node) {
        assert Thread.holdsLock(GLOBAL_CHILD_LOCK);
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
            Handle<T> handle = handleMap.computeIfAbsent(type,
                    aClass -> new Handle<>((Class<T>) aClass));
            handle.invalidate();
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

    @SuppressWarnings("unchecked")
    final class Handle<E extends Event> implements ListenerHandle<E> {
        private final Class<E> eventType;
        private Consumer<E> listener = null;
        private volatile boolean updated;

        Handle(Class<E> eventType) {
            this.eventType = eventType;
        }

        @Override
        public void call(@NotNull E event) {
            final Consumer<E> listener = updatedListener();
            if (listener == null) return;
            try {
                listener.accept(event);
            } catch (Throwable e) {
                MinecraftServer.getExceptionManager().handleException(e);
            }
        }

        @Override
        public boolean hasListener() {
            return updatedListener() != null;
        }

        void invalidate() {
            this.updated = false;
        }

        @Nullable Consumer<E> updatedListener() {
            if (updated) return listener;
            synchronized (GLOBAL_CHILD_LOCK) {
                if (updated) return listener;
                final Consumer<E> listener = createConsumer();
                this.listener = listener;
                this.updated = true;
                return listener;
            }
        }

        private @Nullable Consumer<E> createConsumer() {
            var node = (EventNodeImpl<E>) EventNodeImpl.this;
            // Standalone listeners
            List<Consumer<E>> listeners = new ArrayList<>();
            forTargetEvents(eventType, type -> {
                final ListenerEntry<E> entry = node.listenerMap.get(type);
                if (entry != null) {
                    final Consumer<E> result = listenersConsumer(entry);
                    if (result != null) listeners.add(result);
                }
            });
            final Consumer<E>[] listenersArray = listeners.toArray(Consumer[]::new);
            // Mapped
            final Consumer<E> mappedListener = mappedConsumer();
            // Children
            final Consumer<E>[] childrenListeners = node.children.stream()
                    .filter(child -> child.eventType.isAssignableFrom(eventType)) // Invalid event type
                    .sorted(Comparator.comparing(EventNode::getPriority))
                    .map(child -> ((Handle<E>) child.getHandle(eventType)).updatedListener())
                    .filter(Objects::nonNull)
                    .toArray(Consumer[]::new);
            // Empty check
            final BiPredicate<E, Object> predicate = node.predicate;
            final EventFilter<E, ?> filter = node.filter;
            final boolean hasPredicate = predicate != null;
            final boolean hasListeners = listenersArray.length > 0;
            final boolean hasMap = mappedListener != null;
            final boolean hasChildren = childrenListeners.length > 0;
            if (!hasListeners && !hasMap && !hasChildren) {
                // No listener
                return null;
            }
            return e -> {
                // Filtering
                if (hasPredicate) {
                    final Object value = filter.getHandler(e);
                    if (!predicate.test(e, value)) return;
                }
                // Normal listeners
                if (hasListeners) {
                    for (Consumer<E> listener : listenersArray) {
                        listener.accept(e);
                    }
                }
                // Mapped nodes
                if (hasMap) mappedListener.accept(e);
                // Children
                if (hasChildren) {
                    for (Consumer<E> childHandle : childrenListeners) {
                        childHandle.accept(e);
                    }
                }
            };
        }

        /**
         * Create a consumer calling all listeners from {@link EventNode#addListener(EventListener)} and
         * {@link EventNode#register(EventBinding)}.
         * <p>
         * Most computation should ideally be done outside the consumers as a one-time cost.
         */
        private @Nullable Consumer<E> listenersConsumer(@NotNull ListenerEntry<E> entry) {
            final EventListener<E>[] listenersCopy = entry.listeners.toArray(EventListener[]::new);
            final Consumer<E>[] bindingsCopy = entry.bindingConsumers.toArray(Consumer[]::new);
            final boolean listenersEmpty = listenersCopy.length == 0;
            final boolean bindingsEmpty = bindingsCopy.length == 0;
            if (listenersEmpty && bindingsEmpty) return null;
            if (bindingsEmpty && listenersCopy.length == 1) {
                // Only one normal listener
                final EventListener<E> listener = listenersCopy[0];
                return e -> callListener(listener, e);
            }
            // Worse case scenario, try to run everything
            return e -> {
                if (!listenersEmpty) {
                    for (EventListener<E> listener : listenersCopy) {
                        callListener(listener, e);
                    }
                }
                if (!bindingsEmpty) {
                    for (Consumer<E> eConsumer : bindingsCopy) {
                        eConsumer.accept(e);
                    }
                }
            };
        }

        /**
         * Create a consumer handling {@link EventNode#map(Object, EventFilter)}.
         * The goal is to limit the amount of map lookup.
         */
        private @Nullable Consumer<E> mappedConsumer() {
            var node = (EventNodeImpl<E>) EventNodeImpl.this;
            final var mappedNodeCache = node.registeredMappedNode;
            if (mappedNodeCache.isEmpty()) return null;
            Set<EventFilter<E, ?>> filters = new HashSet<>(mappedNodeCache.size());
            Map<Object, Handle<E>> handlers = new WeakHashMap<>(mappedNodeCache.size());
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
            if (filters.isEmpty()) return null;
            final EventFilter<E, ?>[] filterList = filters.toArray(EventFilter[]::new);
            final BiConsumer<EventFilter<E, ?>, E> mapper = (filter, event) -> {
                final Object handler = filter.castHandler(event);
                final Handle<E> handle = handlers.get(handler);
                if (handle != null) handle.call(event);
            };
            // Specialize the consumer depending on the number of filters to avoid looping
            return switch (filterList.length) {
                case 1 -> event -> mapper.accept(filterList[0], event);
                case 2 -> event -> {
                    mapper.accept(filterList[0], event);
                    mapper.accept(filterList[1], event);
                };
                case 3 -> event -> {
                    mapper.accept(filterList[0], event);
                    mapper.accept(filterList[1], event);
                    mapper.accept(filterList[2], event);
                };
                default -> event -> {
                    for (var filter : filterList) {
                        mapper.accept(filter, event);
                    }
                };
            };
        }

        void callListener(@NotNull EventListener<E> listener, E event) {
            var node = (EventNodeImpl<E>) EventNodeImpl.this;
            EventListener.Result result = listener.run(event);
            if (result == EventListener.Result.EXPIRED) {
                node.removeListener(listener);
                invalidate();
            }
        }
    }
}
