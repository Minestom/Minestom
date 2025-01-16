package net.minestom.server.event.entity.projectile;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.EntityInstanceEvent;
import org.jetbrains.annotations.NotNull;

public record ProjectileUncollideEvent(@NotNull Entity projectile) implements EntityInstanceEvent {
    @Override
    public @NotNull Entity entity() {
        return projectile;
    }
}
