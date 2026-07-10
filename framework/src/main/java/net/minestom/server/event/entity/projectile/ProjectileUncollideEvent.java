package net.minestom.server.event.entity.projectile;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.EntityInstanceEvent;

public final class ProjectileUncollideEvent implements EntityInstanceEvent {

    private final Entity projectile;

    public ProjectileUncollideEvent(Entity projectile) {
        this.projectile = projectile;
    }

    @Override
    public Entity getEntity() {
        return projectile;
    }

}
