package net.minestom.server.event.entity.projectile;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import org.jetbrains.annotations.NotNull;

public sealed class ProjectileCollideEvent implements EntityInstanceEvent, CancellableEvent
        permits ProjectileCollideWithBlockEvent, ProjectileCollideWithEntityEvent {

    private final @NotNull EntityProjectile projectile;
    private final @NotNull Pos position;
    private boolean cancelled;

    protected ProjectileCollideEvent(@NotNull EntityProjectile projectile, @NotNull Pos position) {
        this.projectile = projectile;
        this.position = position;
    }

    @Override
    public @NotNull EntityProjectile getEntity() {
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
