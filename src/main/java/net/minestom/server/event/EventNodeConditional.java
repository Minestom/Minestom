package net.minestom.server.event;

import net.minestom.server.event.handler.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class EventNodeConditional<T extends Event, H extends EventHandler> extends EventNodeImpl<T, H> {

    private volatile Predicate<T> predicate;

    protected EventNodeConditional(EventFilter<T, H> filter, Predicate<T> predicate) {
        super(filter);
        this.predicate = predicate;
    }

    @Override
    protected boolean condition(@NotNull T event) {
        return predicate.test(event);
    }

    public @NotNull Predicate<@NotNull T> getPredicate() {
        return predicate;
    }

    public void setPredicate(@NotNull Predicate<@NotNull T> predicate) {
        this.predicate = predicate;
    }
}
