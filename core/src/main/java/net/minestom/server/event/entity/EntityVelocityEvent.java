package net.minestom.server.event.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.event.EntityEvent;
import net.minestom.server.utils.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a velocity is applied to an entity using {@link Entity#setVelocity(Vector)}.
 */
public class EntityVelocityEvent extends EntityEvent implements CancellableEvent {

    private Vector velocity;

    private boolean cancelled;

    public EntityVelocityEvent(@NotNull Entity entity, @NotNull Vector velocity) {
        super(entity);
        this.velocity = velocity;
    }

    /**
     * Gets the enity who will be affected by {@link #getVelocity()}.
     *
     * @return the entity
     */
    @NotNull
    @Override
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

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
