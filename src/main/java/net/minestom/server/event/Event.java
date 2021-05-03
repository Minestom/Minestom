package net.minestom.server.event;

import net.minestom.server.event.handler.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Object which can be listened to by an {@link EventHandler}.
 * <p>
 * Called using {@link EventHandler#callEvent(Class, Event)}.
 */
public class Event {

    public static <T extends EntityEvent> Listener.Builder<T> entity(Class<T> eventType) {
        return new Listener.Builder<>(eventType);
    }

    public static <T extends PlayerEvent> Listener.Builder<T> player(Class<T> eventType) {
        return new Listener.Builder<>(eventType);
    }


    public static class Listener<T extends Event> {

        private final Class<T> eventType;
        private final Set<EventHandler> attach = new CopyOnWriteArraySet<>();
        private final Consumer<T> combined;

        private Listener(Class<T> eventType, @NotNull Consumer<T> combined) {
            this.eventType = eventType;
            this.combined = combined;
        }

        public void attachTo(@NotNull EventHandler handler) {
            final boolean success = this.attach.add(handler);
            if (success) {
                handler.addEventCallback(eventType, combined::accept);
            }
        }

        public void detachFrom(@NotNull EventHandler handler) {
            final boolean success = this.attach.remove(handler);
            if (success) {
                handler.removeEventCallback(eventType, combined::accept);
            }
        }

        public static class Builder<T extends Event> {

            private final Class<T> eventType;

            private List<Function<T, Boolean>> filters = new ArrayList<>();
            private Consumer<T> handler;

            private Builder(Class<T> eventType) {
                this.eventType = eventType;
            }

            public Builder<T> filter(Function<T, Boolean> filter) {
                this.filters.add(filter);
                return this;
            }

            public Builder<T> handler(Consumer<T> handler) {
                this.handler = handler;
                return this;
            }

            public Listener<T> build() {
                return new Listener<>(eventType, event -> {
                    // Filtering
                    if (!filters.isEmpty()) {
                        if (filters.stream().anyMatch(tBooleanFunction -> !tBooleanFunction.apply(event))) {
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

}
