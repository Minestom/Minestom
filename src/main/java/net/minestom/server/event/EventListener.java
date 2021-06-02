package net.minestom.server.event;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class EventListener<T extends Event> {

    private final Consumer<T> combined;

    private EventListener(@NotNull Consumer<T> combined) {
        this.combined = combined;
    }

    public static <T extends Event> EventListener.Builder<T> of(Class<T> eventType) {
        return new EventListener.Builder<>(eventType);
    }

    protected Consumer<T> getCombined() {
        return combined;
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
            return new EventListener<>(event -> {
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
