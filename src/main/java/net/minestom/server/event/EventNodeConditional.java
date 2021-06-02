package net.minestom.server.event;

import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class EventNodeConditional<T extends Event> extends EventNode<T> {

    private volatile Predicate<T> predicate;

    protected EventNodeConditional(Class<T> type, Predicate<T> predicate) {
        super(type);
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
