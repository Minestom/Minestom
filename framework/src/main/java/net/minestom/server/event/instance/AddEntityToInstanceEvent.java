package net.minestom.server.event.instance;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;

/**
 * Called by an Instance when an entity is added to it.
 * Can be used attach data.
 */
public class AddEntityToInstanceEvent implements InstanceEvent, EntityEvent, CancellableEvent {

    private final Instance instance;
    private final Entity entity;

    private boolean cancelled;

    public AddEntityToInstanceEvent(Instance instance, Entity entity) {
        this.instance = instance;
        this.entity = entity;
    }

    @Override
    public Instance getInstance() {
        return instance;
    }

    /**
     * Entity being added.
     *
     * @return the entity being added
     */
    public Entity getEntity() {
        return entity;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
