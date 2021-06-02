package net.minestom.server.event;

import org.jetbrains.annotations.NotNull;

import java.util.function.BiPredicate;

public class EventNodeConditional<T extends Event, H> extends EventNodeImpl<T, H> {

    private volatile BiPredicate<T, H> predicate;

    protected EventNodeConditional(EventFilter<T, H> filter, BiPredicate<T, H> predicate) {
        super(filter);
        this.predicate = predicate;
    }

    @Override
    protected boolean condition(@NotNull T event) {
        return predicate.test(event, filter.getHandler(event));
    }

    public @NotNull BiPredicate<T, H> getPredicate() {
        return predicate;
    }

    public void setPredicate(@NotNull BiPredicate<T, H> predicate) {
        this.predicate = predicate;
    }
}
