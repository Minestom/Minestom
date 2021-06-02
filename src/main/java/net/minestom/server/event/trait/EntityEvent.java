package net.minestom.server.event.trait;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

public interface EntityEvent extends EventTrait {

    /**
     * Gets the entity of this event.
     *
     * @return the entity
     */
    @NotNull Entity getEntity();
}
