package net.minestom.server.event.instance;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.instance.Instance;

/**
 * Called by an Instance when an entity is removed from it.
 */
public class RemoveEntityFromInstanceEvent extends CancellableEvent {

    private final Instance instance;
    private final Entity entity;

    public RemoveEntityFromInstanceEvent(Instance instance, Entity entity) {
        this.instance = instance;
        this.entity = entity;
    }

    /**
     * Entity being removed
     * @return entity being removed
     */
    public Entity getEntity() {
        return entity;
    }

    /**
     * Instance from which the entity is being removed
     * @return instance from which the entity is being removed
     */
    public Instance getInstance() {
        return instance;
    }
}
