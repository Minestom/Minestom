package net.minestom.server.event.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;

public class EntityFireExtinguishEvent implements EntityInstanceEvent, CancellableEvent {

    private final Entity entity;
    private boolean natural;

    private boolean cancelled;

    public EntityFireExtinguishEvent(Entity entity, boolean natural) {
        this.entity = entity;
        this.natural = natural;
    }

    public boolean isNatural() {
        return natural;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public Entity getEntity() {
        return entity;
    }
}
