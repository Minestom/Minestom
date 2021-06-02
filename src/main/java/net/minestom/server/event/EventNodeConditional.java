package net.minestom.server.event;

import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class EventNodeConditional<T extends Event> extends EventNode<T> {

    private volatile Predicate<T> predicate;

    protected EventNodeConditional(Predicate<T> predicate) {
        this.predicate = predicate;
    }

    @Override
    protected boolean isValid(@NotNull T event) {
        return predicate.test(event);
    }

    public Predicate<T> getPredicate() {
        return predicate;
    }

    public void setPredicate(Predicate<T> predicate) {
        this.predicate = predicate;
    }
}
