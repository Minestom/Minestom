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

    /**
     * @return the entity who the velocity is applied to
     */
    public Entity getEntity() {
        return entity;
    }

    /**
     * @return the velocity which will be applied
     */
    public Vector getVelocity() {
        return velocity;
    }

    /**
     * @param velocity the new velocity to applies
     */
    public void setVelocity(Vector velocity) {
        this.velocity = velocity;
    }
}
