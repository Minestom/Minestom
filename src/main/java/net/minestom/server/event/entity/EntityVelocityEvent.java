package net.minestom.server.event.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.utils.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a velocity is applied to an entity using {@link Entity#setVelocity(Vector)}.
 */
public class EntityVelocityEvent extends CancellableEvent {

    private final Entity entity;
    private Vector velocity;

    public EntityVelocityEvent(@NotNull Entity entity, @NotNull Vector velocity) {
        this.entity = entity;
        this.velocity = velocity;
    }

    /**
     * Gets the enity who will be affected by {@link #getVelocity()}.
     *
     * @return the entity
     */
    @NotNull
    public Entity getEntity() {
        return entity;
    }

    /**
     * Gets the velocity which will be applied.
     *
     * @return the velocity
     */
    @NotNull
    public Vector getVelocity() {
        return velocity;
    }

    /**
     * Changes the applied velocity.
     *
     * @param velocity the new velocity
     */
    public void setVelocity(@NotNull Vector velocity) {
        this.velocity = velocity;
    }
}
