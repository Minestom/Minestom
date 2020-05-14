package net.minestom.server.event.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.utils.Vector;

public class EntityVelocityEvent extends CancellableEvent {

    private Entity entity;
    private Vector velocity;

    public EntityVelocityEvent(Entity entity, Vector velocity) {
        this.entity = entity;
        this.velocity = velocity;
    }

    public Entity getEntity() {
        return entity;
    }

    public Vector getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector velocity) {
        this.velocity = velocity;
    }
}
