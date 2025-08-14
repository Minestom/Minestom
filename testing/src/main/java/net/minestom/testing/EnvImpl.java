package net.minestom.testing;

import net.minestom.server.ServerProcess;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventListener;

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

        // Start the dispatcher threads if not already started.
        process().dispatcher().start();

        // Use player provider to disable queued chunk sending.
        // Set here to allow an individual test to override if they want.
        process.connection().setPlayerProvider(TestConnectionImpl.TestPlayerImpl::new);
    }

    @Override
    public ServerProcess process() {
        return process;
    }

    @Override
    public TestConnection createConnection() {
        return new TestConnectionImpl(this);
    }

    @Override
    public <E extends Event, H> Collector<E> trackEvent(Class<E> eventType, EventFilter<? super E, H> filter, H actor) {
        var tracker = new EventCollector<E>(actor);
        this.process.eventHandler().map(actor, filter).addListener(eventType, tracker.events::add);
        return tracker;
    }

    @Override
    public <E extends Event> FlexibleListener<E> listen(Class<E> eventType) {
        var handler = process.eventHandler();
        var flexible = new FlexibleListenerImpl<>(eventType);
        var listener = EventListener.of(eventType, e -> flexible.handler.accept(e));
        handler.addListener(listener);
        this.listeners.add(flexible);
        return flexible;
    }

    void cleanup() {
        this.listeners.forEach(FlexibleListenerImpl::check);
        this.process.stop();
    }

    final class EventCollector<E extends Event> implements Collector<E> {
        private final Object handler;
        private final List<E> events = new CopyOnWriteArrayList<>();

        public EventCollector(Object handler) {
            this.handler = handler;
        }

        @Override
        public List<E> collect() {
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
        public void followup(Consumer<E> handler) {
            updateHandler(handler);
        }

        @Override
        public void failFollowup() {
            updateHandler(e -> fail("Event " + e.getClass().getSimpleName() + " was not expected"));
        }

        void updateHandler(Consumer<E> handler) {
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
