package net.minestom.server.event.trait.mutation;

import net.minestom.server.event.trait.MutableEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface EventMutator<T extends MutableEvent<T>> {

    /**
     * Returns a newly constructed event.
     *
     * @return T the new instance.
     */
    @Contract(pure = true)
    @NotNull T mutated();
}
