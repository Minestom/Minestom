package net.minestom.server.event.world;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.event.WorldEvent;
import net.minestom.server.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Called by a World when an entity is removed from it.
 */
public class RemoveEntityFromWorldEvent extends WorldEvent implements CancellableEvent {

    private final Entity entity;

    private boolean cancelled;

    public RemoveEntityFromWorldEvent(@NotNull World world, @NotNull Entity entity) {
        super(world);
        this.entity = entity;
    }

    /**
     * Gets the entity being removed.
     *
     * @return entity being removed
     */
    @NotNull
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
