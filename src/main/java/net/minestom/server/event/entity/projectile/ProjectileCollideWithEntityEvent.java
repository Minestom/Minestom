package net.minestom.server.event.entity.projectile;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;

public final class ProjectileCollideWithEntityEvent extends ProjectileCollideEvent {

    private final Entity target;

    public ProjectileCollideWithEntityEvent(
            Entity projectile,
            Pos position,
            Entity target
    ) {
        super(projectile, position);
        this.target = target;
    }

    public Entity getTarget() {
        return target;
    }
}
