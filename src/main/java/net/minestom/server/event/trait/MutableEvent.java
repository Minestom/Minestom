package net.minestom.server.event.trait;

import net.minestom.server.event.Event;
import net.minestom.server.event.trait.mutation.EventMutator;
import org.jetbrains.annotations.NotNull;

public interface MutableEvent<E extends MutableEvent<E>> extends Event {

    /**
     * Gets the {@link EventMutator} for this event.
     *
     * @return the mutator
     */
    @NotNull EventMutator<E> mutator();
}
