package net.minestom.server.event;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventNodeList<T extends Event, H> extends EventNode<T> {

    private final List<H> entries = new CopyOnWriteArrayList<>();

    protected EventNodeList(EventFilter<T, H> filter) {
        super(filter);
    }

    @Override
    protected boolean condition(@NotNull T event) {
        final var eventHandler = filter.getHandler(event);
        return entries.contains(eventHandler);
    }

    public void addEntry(@NotNull H handler) {
        this.entries.add(handler);
    }

    public void removeEntry(@NotNull H handler) {
        this.entries.remove(handler);
    }
}
