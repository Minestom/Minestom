package net.minestom.testing;

import net.minestom.server.event.Event;
import net.minestom.server.event.trait.mutation.EventMutator;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;

public interface FlexibleListener<E extends Event> {
    /**
     * Updates the handler. Fails if the previous followup has not been called.
     */
    void followup(@NotNull Consumer<E> handler);

    /**
     * Updates the handler. Fails if the previous followup has not been called.
     */
    void followup(@NotNull Function<E, EventMutator<?>> handler);

    default void followup() {
        followup(event -> {
            // Empty
        });
    }

    /**
     * Fails if an event is received. Valid until the next followup call.
     */
    void failFollowup();
}
