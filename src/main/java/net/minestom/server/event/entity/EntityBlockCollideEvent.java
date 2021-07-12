package net.minestom.server.event.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerItemAnimationEvent;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * This event is called when an {@link Entity} collides with a block.
 */
public class EntityBlockCollideEvent implements EntityEvent, InstanceEvent, CancellableEvent {

    private final Entity entity;
    private final Instance instance;
    private final Vector direction;

    private boolean cancelled;

    public EntityBlockCollideEvent(@NotNull Entity entity, @NotNull Instance instance, @NotNull Vector direction) {
        this.entity = entity;
        this.instance = instance;
        this.direction = direction;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    /**
     * @return entity the entity that collided with a block
     */
    @Override
    public @NotNull Entity getEntity() {
        return entity;
    }

    /**
     * @return instance the instance that the collision occurs in
     */
    @Override
    public @NotNull Instance getInstance() {
        return instance;
    }

    /**
     * @return direction the direction of the collided block from the entity
     */
    public @NotNull Vector getCollisionDirection() {
        return direction;
    }
}
