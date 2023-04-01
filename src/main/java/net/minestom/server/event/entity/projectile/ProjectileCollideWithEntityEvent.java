package net.minestom.server.event.entity.projectile;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

public final class ProjectileCollideWithEntityEvent extends ProjectileCollideEvent {

    private final @NotNull Entity target;

    public ProjectileCollideWithEntityEvent(@NotNull Entity projectile, @NotNull Point position,
                                            @NotNull Entity target) {
        super(projectile, position);
        this.target = target;
    }

    public @NotNull Entity getTarget() {
        return target;
    }
}
