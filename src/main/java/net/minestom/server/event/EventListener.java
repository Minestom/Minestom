package net.minestom.server.event;

import net.minestom.server.utils.time.UpdateOption;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class EventListener<T extends Event> {

    protected final Class<T> type;
    protected final Function<T, Result> executor;

    private EventListener(@NotNull Class<T> type, @NotNull Function<T, Result> executor) {
        this.type = type;
        this.executor = executor;
    }

    public static <T extends Event> EventListener.Builder<T> of(@NotNull Class<T> eventType) {
        return new EventListener.Builder<>(eventType);
    }

    public static class Builder<T extends Event> {

        private final Class<T> eventType;

        private final List<Predicate<T>> filters = new ArrayList<>();
        private int expirationCount;
        private UpdateOption expirationTime;
        private Consumer<T> handler;

        protected Builder(Class<T> eventType) {
            this.eventType = eventType;
        }

        public EventListener.Builder<T> filter(Predicate<T> filter) {
            this.filters.add(filter);
            return this;
        }

        public EventListener.Builder<T> expirationCount(int expirationCount) {
            this.expirationCount = expirationCount;
            return this;
        }

        public EventListener.Builder<T> expirationTime(UpdateOption expirationTime) {
            this.expirationTime = expirationTime;
            return this;
        }

        public EventListener.Builder<T> handler(Consumer<T> handler) {
            this.handler = handler;
            return this;
        }

        public EventListener<T> build() {
            AtomicInteger expirationCount = new AtomicInteger(this.expirationCount);
            final boolean hasExpirationCount = expirationCount.get() > 0;

            final var filters = new ArrayList<>(this.filters);
            final var handler = this.handler;
            return new EventListener<>(eventType, event -> {
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
                // Expiration check
                if (hasExpirationCount && expirationCount.decrementAndGet() == 0) {
                    return Result.EXPIRED;
                }
                return Result.SUCCESS;
            });
        }
    }


    enum Result {
        SUCCESS,
        INVALID,
        EXPIRED
    }
}
