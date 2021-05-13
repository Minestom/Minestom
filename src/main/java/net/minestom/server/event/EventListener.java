package net.minestom.server.event;

import net.minestom.server.event.handler.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class EventListener<T extends Event> implements ListenerAttach {

    private final Class<T> eventType;
    private final Set<EventHandler> attach = new CopyOnWriteArraySet<>();
    private final Consumer<T> combined;

    private EventListener(Class<T> eventType, @NotNull Consumer<T> combined) {
        this.eventType = eventType;
        this.combined = combined;
    }

    @Override
    public void attachTo(@NotNull EventHandler handler) {
        final boolean success = this.attach.add(handler);
        if (success) {
            handler.addEventCallback(eventType, combined::accept);
        }
    }

    @Override
    public void detachFrom(@NotNull EventHandler handler) {
        final boolean success = this.attach.remove(handler);
        if (success) {
            handler.removeEventCallback(eventType, combined::accept);
        }
    }

    public static class Builder<T extends Event> {

        private final Class<T> eventType;

        private List<Predicate<T>> filters = new ArrayList<>();
        private Consumer<T> handler;

        protected Builder(Class<T> eventType) {
            this.eventType = eventType;
        }

        public EventListener.Builder<T> filter(Predicate<T> filter) {
            this.filters.add(filter);
            return this;
        }

        public EventListener.Builder<T> handler(Consumer<T> handler) {
            this.handler = handler;
            return this;
        }

        public EventListener<T> build() {
            return new EventListener<>(eventType, event -> {
                // Filtering
                if (!filters.isEmpty()) {
                    if (filters.stream().anyMatch(filter -> !filter.test(event))) {
                        // Cancelled
                        return;
                    }
                }

                // Handler
                if (handler != null) {
                    handler.accept(event);
                }
            });
        }
    }
}
