package net.minestom.server.event;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface EventListener<T extends Event> {

    @NotNull Class<T> getEventType();

    @NotNull Result run(@NotNull T event);

    @Contract(pure = true)
    static <T extends Event> EventListener.@NotNull Builder<T> builder(@NotNull Class<T> eventType) {
        return new EventListener.Builder<>(eventType);
    }

    @Contract(pure = true)
    static <T extends Event> @NotNull EventListener<T> of(@NotNull Class<T> eventType, @NotNull Consumer<@NotNull T> listener) {
        return new EventListener<>() {
            @Override
            public @NotNull Class<T> getEventType() {
                return eventType;
            }

            @Override
            public @NotNull Result run(@NotNull T event) {
                listener.accept(event);
                return Result.SUCCESS;
            }
        };
    }

    class Builder<T extends Event> {
        private final Class<T> eventType;
        private final List<Predicate<T>> filters = new ArrayList<>();
        private int expireCount;
        private Predicate<T> expireWhen;
        private Consumer<T> handler;

        protected Builder(Class<T> eventType) {
            this.eventType = eventType;
        }

        @Contract(value = "_ -> this")
        public @NotNull EventListener.Builder<T> filter(Predicate<T> filter) {
            this.filters.add(filter);
            return this;
        }

        @Contract(value = "_ -> this")
        public @NotNull EventListener.Builder<T> expireCount(int expireCount) {
            this.expireCount = expireCount;
            return this;
        }

        @Contract(value = "_ -> this")
        public @NotNull EventListener.Builder<T> expireWhen(Predicate<T> expireWhen) {
            this.expireWhen = expireWhen;
            return this;
        }

        @Contract(value = "_ -> this")
        public @NotNull EventListener.Builder<T> handler(Consumer<T> handler) {
            this.handler = handler;
            return this;
        }

        @Contract(value = "-> new", pure = true)
        public @NotNull EventListener<T> build() {
            AtomicInteger expirationCount = new AtomicInteger(this.expireCount);
            final boolean hasExpirationCount = expirationCount.get() > 0;

            final Predicate<T> expireWhen = this.expireWhen;

            final var filters = new ArrayList<>(this.filters);
            final var handler = this.handler;
            return new EventListener<>() {
                @Override
                public @NotNull Class<T> getEventType() {
                    return eventType;
                }

                @Override
                public @NotNull Result run(@NotNull T event) {
                    // Expiration predicate
                    if (expireWhen != null && expireWhen.test(event)) {
                        return Result.EXPIRED;
                    }
                    // Filtering
                    if (!filters.isEmpty()) {
                        if (filters.stream().anyMatch(filter -> !filter.test(event))) {
                            // Cancelled
                            return Result.INVALID;
                        }
                    }
                    // Handler
                    if (handler != null) {
                        handler.accept(event);
                    }
                    // Expiration count
                    if (hasExpirationCount && expirationCount.decrementAndGet() == 0) {
                        return Result.EXPIRED;
                    }
                    return Result.SUCCESS;
                }
            };
        }
    }

    enum Result {
        SUCCESS,
        INVALID,
        EXPIRED
    }
}
