package net.minestom.server.event.entity;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a velocity is applied to an entity using {@link Entity#setVelocity(Vec)}.
 */
public class EntityVelocityEvent implements EntityInstanceEvent, CancellableEvent {

    private final Entity entity;
    private Vec velocity;

    private boolean cancelled;

    public EntityVelocityEvent(@NotNull Entity entity, @NotNull Vec velocity) {
        this.entity = entity;
        this.velocity = velocity;
    }

    /**
     * Gets the enity who will be affected by {@link #getVelocity()}.
     *
     * @return the entity
     */
    @Override
    public @NotNull Entity getEntity() {
        return entity;
    }

    /**
     * Gets the velocity which will be applied.
     *
     * @return the velocity
     */
    public @NotNull Vec getVelocity() {
        return velocity;
    }

    /**
     * Changes the applied velocity.
     *
     * @param velocity the new velocity
     */
    public void setVelocity(@NotNull Vec velocity) {
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
