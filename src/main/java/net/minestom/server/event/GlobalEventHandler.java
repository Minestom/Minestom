package net.minestom.server.event;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Object containing all the global event listeners.
 */
public final class GlobalEventHandler extends EventNodeImpl<Event> {
    public GlobalEventHandler() {
        super("global", EventFilter.ALL, null);
    }

    @Override
    protected @NotNull ListenerHandle<Event> createHandle(@NotNull Class<Event> listenerType) {
        return new GlobalHandle<>(listenerType);
    }

    @SuppressWarnings("unchecked")
    final class GlobalHandle<E extends Event> extends Handle<E> {
        // Represents the filters where the handler has a node
        private static final List<EventFilter<?, ?>> HANDLER_FILTERS = List.of(EventFilter.ENTITY);
        // Local nodes handling
        private final EventFilter<E, EventHandler<? super E>>[] localFilters;

        GlobalHandle(Class<E> eventType) {
            super(eventType);
            // Filters with EventHandler support
            this.localFilters = (EventFilter<E, EventHandler<? super E>>[]) HANDLER_FILTERS.stream()
                    .filter(filter -> filter.eventType().isAssignableFrom(eventType)).toArray(EventFilter[]::new);
        }

        @Override
        public void call(@NotNull E event) {
            // Per-handler listeners
            for (var filter : localFilters) {
                var handle = filter.getHandler(event);
                if (handle != null) handle.eventNode().call(event);
            }
            // Global listeners
            super.call(event);
        }
    }
}
