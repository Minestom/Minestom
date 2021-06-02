package net.minestom.server.event;

import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class EventNodeConditional<T extends Event> extends EventNode<T> {

    private final Class<T> type;
    private volatile Predicate<T> predicate;

    protected EventNodeConditional(Class<T> type, Predicate<T> predicate) {
        this.type = type;
        this.predicate = predicate;
    }

    @Override
    protected boolean isValid(@NotNull T event) {
        final boolean typeCheck = type.isAssignableFrom(event.getClass());
        return typeCheck && predicate.test(event);
    }

    public @NotNull Predicate<@NotNull T> getPredicate() {
        return predicate;
    }

    public void setPredicate(@NotNull Predicate<@NotNull T> predicate) {
        this.predicate = predicate;
    }
}
