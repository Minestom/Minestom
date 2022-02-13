package net.minestom.server.api;

import net.minestom.server.ServerProcess;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

final class EnvImpl implements Env {
    private final ServerProcess process;
    private final List<FlexibleListenerImpl<?>> listeners = new CopyOnWriteArrayList<>();

    public EnvImpl(ServerProcess process) {
        this.process = process;
    }

    @Override
    public @NotNull ServerProcess process() {
        return process;
    }

    @Override
    public @NotNull TestConnection createConnection() {
        return new TestConnectionImpl(this);
    }

    @Override
    public @NotNull <E extends Event, H> Collector<E> trackEvent(@NotNull Class<E> eventType, @NotNull EventFilter<? super E, H> filter, @NotNull H actor) {
        var tracker = new EventCollector<E>(actor);
        var node = EventNode.type("tracker", filter).addListener(eventType, tracker.events::add);
        process.eventHandler().map(node, actor);
        return tracker;
    }

    @Override
    public @NotNull <E extends Event> FlexibleListener<E> listen(@NotNull Class<E> eventType) {
        var handler = process.eventHandler();
        var flexible = new FlexibleListenerImpl<>(eventType);
        var listener = EventListener.of(eventType, e -> flexible.handler.accept(e));
        handler.addListener(listener);
        this.listeners.add(flexible);
        return flexible;
    }

    void cleanup() {
        this.listeners.forEach(FlexibleListenerImpl::check);
    }

    final class EventCollector<E extends Event> implements Collector<E> {
        private final Object handler;
        private final List<E> events = new CopyOnWriteArrayList<>();

        public EventCollector(Object handler) {
            this.handler = handler;
        }

        @Override
        public @NotNull List<E> collect() {
            process.eventHandler().unmap(handler);
            return List.copyOf(events);
        }
    }

    static final class FlexibleListenerImpl<E extends Event> implements FlexibleListener<E> {
        private final Class<E> eventType;
        private Consumer<E> handler = e -> {
        };
        private boolean initialized;
        private boolean called;

        FlexibleListenerImpl(Class<E> eventType) {
            this.eventType = eventType;
        }

        @Override
        public void followup(@NotNull Consumer<E> handler) {
            updateHandler(handler);
        }

        @Override
        public void failFollowup() {
            updateHandler(e -> fail("Event " + e.getClass().getSimpleName() + " was not expected"));
        }

        void updateHandler(@NotNull Consumer<E> handler) {
            check();
            this.initialized = true;
            this.called = false;
            this.handler = e -> {
                handler.accept(e);
                this.called = true;
            };
        }

        void check() {
            assertTrue(!initialized || called, "Last listener has not been called: " + eventType.getSimpleName());
        }
    }
}
