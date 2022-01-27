package net.minestom.server.api;

import net.minestom.server.ServerProcess;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

final class EnvImpl implements Env {
    private final ServerProcess process;

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
}
