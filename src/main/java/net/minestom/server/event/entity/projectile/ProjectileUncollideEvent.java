package net.minestom.server.event.entity.projectile;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.EntityInstanceEvent;
import org.jetbrains.annotations.NotNull;

public final class ProjectileUncollideEvent implements EntityInstanceEvent {

    private final @NotNull Entity projectile;

    public ProjectileUncollideEvent(@NotNull Entity projectile) {
        this.projectile = projectile;
    }

    @Override
    public @NotNull Entity getEntity() {
        return projectile;
    }

}
