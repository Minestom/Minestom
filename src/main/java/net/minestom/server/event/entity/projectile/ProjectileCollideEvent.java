package net.minestom.server.event.entity.projectile;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.event.trait.RecursiveEvent;

class ProjectileCollideEvent implements EntityInstanceEvent, CancellableEvent, RecursiveEvent {

    private final Entity projectile;
    private final Pos position;
    private boolean cancelled;

    protected ProjectileCollideEvent(Entity projectile, Pos position) {
        this.projectile = projectile;
        this.position = position;
    }

    @Override
    public Entity getEntity() {
        return projectile;
    }

    public Pos getCollisionPosition() {
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
