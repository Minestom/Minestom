package net.minestom.server.event.entity.projectile;

import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.event.trait.EntityInstanceEvent;
import org.jetbrains.annotations.NotNull;

public final class ProjectileUncollideEvent implements EntityInstanceEvent {

    private final @NotNull EntityProjectile projectile;

    public ProjectileUncollideEvent(@NotNull EntityProjectile projectile) {
        this.projectile = projectile;
    }

    @Override
    public @NotNull EntityProjectile getEntity() {
        return projectile;
    }
}
