package net.minestom.server.event.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.EntityInstanceEvent;

/**
 * Called right before an entity is removed
 */
public class EntityDespawnEvent implements EntityInstanceEvent {

    private final Entity entity;

    public EntityDespawnEvent(Entity entity) {
        this.entity = entity;
    }

    /**
     * Gets the entity who is about to be removed
     *
     * @return the entity
     */
    @Override
    public Entity getEntity() {
        return entity;
    }
}
