package net.minestom.server.event.entity.projectile;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.event.trait.RecursiveEvent;
import org.jetbrains.annotations.NotNull;

class ProjectileCollideEvent implements EntityInstanceEvent, CancellableEvent, RecursiveEvent {

    private final @NotNull Entity projectile;
    private final @NotNull Pos position;
    private boolean cancelled;

    protected ProjectileCollideEvent(@NotNull Entity projectile, @NotNull Pos position) {
        this.projectile = projectile;
        this.position = position;
    }

    @Override
    public @NotNull Entity getEntity() {
        return projectile;
    }

    public @NotNull Pos getPosition() {
        return position;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }
}
