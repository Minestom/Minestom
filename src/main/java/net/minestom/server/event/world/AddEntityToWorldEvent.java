package net.minestom.server.event.world;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.event.WorldEvent;
import net.minestom.server.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Called by a World when an entity is added to it.
 * Can be used attach data.
 */
public class AddEntityToWorldEvent extends WorldEvent implements CancellableEvent {

    private final Entity entity;

    private boolean cancelled;

    public AddEntityToWorldEvent(@NotNull World world, @NotNull Entity entity) {
        super(world);
        this.entity = entity;
    }

    /**
     * Entity being added.
     *
     * @return the entity being added
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
