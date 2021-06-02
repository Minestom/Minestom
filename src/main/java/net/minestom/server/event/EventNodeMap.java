package net.minestom.server.event;

import net.minestom.server.event.handler.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

public class EventNodeMap<T extends Event, H extends EventHandler> extends EventNodeImpl<T> {

    private final Function<T, H> handlerGetter;

    private final List<H> entries = new CopyOnWriteArrayList<>();

    protected EventNodeMap(Class<T> eventType, Function<T, H> handlerGetter) {
        super(eventType);
        this.handlerGetter = handlerGetter;
    }

    @Override
    protected boolean condition(@NotNull T event) {
        final var eventHandler = handlerGetter.apply(event);
        return entries.contains(eventHandler);
    }

    public void addEntry(@NotNull H handler) {
        this.entries.add(handler);
    }

    public void removeEntry(@NotNull H handler) {
        this.entries.remove(handler);
    }
}
