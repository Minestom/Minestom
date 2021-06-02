package net.minestom.server.event.trait;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;

public interface EntityEvent extends Event {

    /**
     * Gets the entity of this event.
     *
     * @return the entity
     */
    @NotNull Entity getEntity();
}
