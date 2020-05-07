package net.minestom.server.event.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;

/**
 * Called when an entity ticks itself.
 * Same instance used for all tick events for the same entity
 */
public class EntityTickEvent extends Event {

    private final Entity entity;

    public EntityTickEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }
}
