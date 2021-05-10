package net.minestom.server.event.instance;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.event.InstanceEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

/**
 * Called by an Instance when an entity is removed from it.
 */
public class RemoveEntityFromInstanceEvent extends InstanceEvent implements CancellableEvent {

    private final Entity entity;

    private boolean cancelled;

    public RemoveEntityFromInstanceEvent(@NotNull Instance instance, @NotNull Entity entity) {
        super(instance);
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
