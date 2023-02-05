package net.minestom.server.event.entity;

import org.jetbrains.annotations.NotNull;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;

public class EntityTeleportEvent implements EntityInstanceEvent, CancellableEvent {

    protected final Entity entity;
    protected final @NotNull Pos position;
    protected boolean cancelled = false;

    public EntityTeleportEvent(@NotNull Entity entity, @NotNull Pos position) {
        this.entity = entity;
        this.position = position;
    }

    @Override
    public @NotNull Entity getEntity() {
        return entity;
    }
    
    public Pos getPosition() {
    	return position;
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
