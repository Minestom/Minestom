package net.minestom.server.event.instance;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.instance.Instance;

/**
 * Called by an Instance when an entity is removed from it.
 */
public class RemoveEntityFromInstanceEvent implements EntityInstanceEvent {
    private final Instance instance;
    private final Entity entity;

    public RemoveEntityFromInstanceEvent(Instance instance, Entity entity) {
        this.instance = instance;
        this.entity = entity;
    }

    @Override
    public Instance getInstance() {
        return instance;
    }

    /**
     * Gets the entity being removed.
     *
     * @return entity being removed
     */
    public Entity getEntity() {
        return entity;
    }
}
