package net.minestom.server.event;

import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.MutableEvent;
import net.minestom.server.event.trait.mutation.EventMutator;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Represents an event listener (handler) in an event graph.
 * <p>
 * A listener is responsible for executing some action based on an event triggering.
 *
 * @param <T> The event type being handled.
 */
public interface EventListener<T extends Event> {

    @NotNull Class<T> eventType();

    @NotNull Result<T> run(@NotNull T event);

    boolean isMutator();

    @Contract(pure = true)
    static <T extends Event> EventListener.@NotNull Builder<T> builder(@NotNull Class<T> eventType) {
        return new EventListener.Builder<>(eventType);
    }

    /**
     * Create an event listener without any special options. The given listener will be executed
     * if the event passes all parent filtering.
     *
     * @param eventType The event type to handle
     * @param listener  The handler function
     * @param <T>       The event type to handle
     * @return An event listener with the given properties
     */
    @Contract(pure = true)
    static <T extends Event> @NotNull EventListener<T> of(@NotNull Class<T> eventType, @NotNull Consumer<@NotNull T> listener) {
        return builder(eventType).handler(new Handler.ConsumerHandler<>(listener)).build();
    }

    /**
     * Create an mutable event listener without any special options. The given listener will be executed
     * if the event passes all parent filtering.
     *
     * @param eventType The event type to handle
     * @param listener  The handler function
     * @param <T>       The event type to handle
     * @return An event listener with the given properties
     */
    @Contract(pure = true)
    static <T extends MutableEvent<T> & Event> @NotNull EventListener<T> of(@NotNull Class<T> eventType, @NotNull Function<@NotNull T, @NotNull EventMutator<T>> listener) {
        return builder(eventType).handler(new Handler.FunctionHandler<>(listener.andThen(EventMutator::mutated))).build();
    }

    class Builder<T extends Event> {
        private final Class<T> eventType;
        private final List<Predicate<T>> filters = new ArrayList<>();
        private boolean ignoreCancelled = true;
        private int expireCount;
        private Predicate<T> expireWhen;
        private Handler<T> handler;

        protected Builder(Class<T> eventType) {
            this.eventType = eventType;
        }

        /**
         * Adds a filter to the executor of this listener. The executor will only
         * be called if this condition passes on the given event.
         */
        @Contract(value = "_ -> this")
        public @NotNull EventListener.Builder<T> filter(Predicate<T> filter) {
            this.filters.add(filter);
            return this;
        }

        /**
         * Specifies if the handler should still be called if {@link CancellableEvent#cancelled()} returns {@code true}.
         * <p>
         * Default is set to {@code true}.
         *
         * @param ignoreCancelled True to stop processing the event when cancelled
         */
        @Contract(value = "_ -> this")
        public @NotNull EventListener.Builder<T> ignoreCancelled(boolean ignoreCancelled) {
            this.ignoreCancelled = ignoreCancelled;
            return this;
        }

        /**
         * Removes this listener after it has been executed the given number of times.
         *
         * @param expireCount The number of times to execute
         */
        @Contract(value = "_ -> this")
        public @NotNull EventListener.Builder<T> expireCount(int expireCount) {
            this.expireCount = expireCount;
            return this;
        }

        /**
         * Expires this listener when it passes the given condition. The expiration will
         * happen before the event is executed.
         *
         * @param expireWhen The condition to test
         */
        @Contract(value = "_ -> this")
        public @NotNull EventListener.Builder<T> expireWhen(Predicate<T> expireWhen) {
            this.expireWhen = expireWhen;
            return this;
        }

        /**
         * Sets the handler for this event listener. This will be executed if the listener passes
         * all conditions.
         */
        @Contract(value = "_ -> this")
        public @NotNull EventListener.Builder<T> handler(Handler<T> handler) {
            this.handler = handler;
            return this;
        }

        @Contract(value = "-> new", pure = true)
        public @NotNull EventListener<T> build() {
            final boolean ignoreCancelled = this.ignoreCancelled;
            AtomicInteger expirationCount = new AtomicInteger(this.expireCount);
            final boolean hasExpirationCount = expirationCount.get() > 0;

            final Predicate<T> expireWhen = this.expireWhen;

            final var filters = new ArrayList<>(this.filters);
            final var handler = this.handler;
            final var canMutate = handler instanceof Handler.FunctionHandler<T>;
            return new EventListener<>() {
                @Override
                public @NotNull Class<T> eventType() {
                    return eventType;
                }

                @Override
                public @NotNull Result<T> run(@NotNull T event) {
                    // Event cancellation
                    if (ignoreCancelled && event instanceof CancellableEvent<?> cancellableEvent &&
                            cancellableEvent.cancelled()) {
                        return (Result<T>) Result.INVALID;
                    }
                    // Expiration predicate
                    if (expireWhen != null && expireWhen.test(event)) {
                        return (Result<T>) Result.EXPIRED;
                    }
                    // Filtering
                    if (!filters.isEmpty()) {
                        for (var filter : filters) {
                            if (!filter.test(event)) {
                                // Cancelled
                                return (Result<T>) Result.INVALID;
                            }
                        }
                    }
                    // Handler
                    if (handler != null) {
                        if (handler instanceof Handler.FunctionHandler<T>(Function<T, T> handlerHolder)) {
                            return new Result<>(handlerHolder.apply(event));
                        } else if (handler instanceof Handler.ConsumerHandler<T>(Consumer<T> handlerHolder)) {
                            handlerHolder.accept(event);
                        }
                    }
                    // Expiration count
                    if (hasExpirationCount && expirationCount.decrementAndGet() == 0) {
                        return (Result<T>) Result.EXPIRED;
                    }
                    return (Result<T>) Result.SUCCESS;
                }

                @Override
                public boolean isMutator() {
                    return canMutate;
                }
            };
        }
    }

    sealed interface Handler<T extends Event> {
        record FunctionHandler<T extends Event>(Function<T, T> handler) implements Handler<T> {}
        record ConsumerHandler<T extends Event>(Consumer<T> handler) implements Handler<T> {}
    }

    // Dirty way to sometimes pass data back.
    record Result<T extends Event>(T data) {
        public static final Result<?> SUCCESS = new Result<>(null);
        public static final Result<?> INVALID = new Result<>(null);
        public static final Result<?> EXPIRED = new Result<>(null);
        public static final Result<?> EXCEPTION = new Result<>(null);
    }
}
