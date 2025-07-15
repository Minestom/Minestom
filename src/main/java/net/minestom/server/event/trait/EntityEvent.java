package net.minestom.server.event.trait;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;

/**
 * Represents any event called on an {@link Entity}.
 */
public interface EntityEvent extends Event {

    /**
     * Gets the entity of this event.
     *
     * @return the entity
     */
    Entity getEntity();
}
