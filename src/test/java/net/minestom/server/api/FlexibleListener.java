package net.minestom.server.api;

import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface FlexibleListener<E extends Event> {

    void setHandler(@NotNull Consumer<E> handler);

    /**
     * Updates the handler. Fails if the previous followup has not been called.
     */
    void followup(@NotNull Consumer<E> handler);
}
