package net.minestom.server.event;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public final class EventInterface<T> {

    public static <T> @NotNull Builder<T> builder(@NotNull Class<T> type) {
        return new Builder<>();
    }

    final Map<Class<? extends Event>, BiConsumer<T, Event>> mapped;

    EventInterface(Map<Class<? extends Event>, BiConsumer<T, Event>> map) {
        this.mapped = map;
    }

    public static class Builder<T> {
        private final Map<Class<? extends Event>, BiConsumer<T, Event>> mapped = new HashMap<>();

        @SuppressWarnings("unchecked")
        public <E extends Event> Builder<T> map(@NotNull Class<E> eventType,
                                                @NotNull BiConsumer<@NotNull T, @NotNull E> consumer) {
            this.mapped.put(eventType, (BiConsumer<T, Event>) consumer);
            return this;
        }

        public @NotNull EventInterface<T> build() {
            return new EventInterface<>(Map.copyOf(mapped));
        }
    }
}
