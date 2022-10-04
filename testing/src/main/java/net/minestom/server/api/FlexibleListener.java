package net.minestom.server.api;

import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface FlexibleListener<E extends Event> {
    /**
     * Updates the handler. Fails if the previous followup has not been called.
     */
    void followup(@NotNull Consumer<E> handler);

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
