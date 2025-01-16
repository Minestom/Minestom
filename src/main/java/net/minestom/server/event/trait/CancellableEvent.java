package net.minestom.server.event.trait;

import net.minestom.server.event.Event;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.trait.mutation.EventMutator;
import net.minestom.server.event.trait.mutation.EventMutatorCancellable;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an {@link Event} which can be cancelled.
 * Called using {@link EventDispatcher#callCancellable(CancellableEvent, Runnable)}.
 */
public interface CancellableEvent<E extends CancellableEvent<E>> extends MutableEvent<E>, Event {

    /**
     * Gets if the {@link Event} is cancelled or not.
     */
    boolean cancelled();

    /**
     * Gets the {@link EventMutatorCancellable} for this event.
     *
     * @return the mutator
     */
    @Override
    @NotNull
    EventMutatorCancellable<E> mutator();
}
