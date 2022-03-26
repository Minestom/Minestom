package net.minestom.server.event.entity.projectile;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

public final class ProjectileCollideWithEntityEvent extends ProjectileCollideEvent {

    private final @NotNull LivingEntity target;

    public ProjectileCollideWithEntityEvent(
            @NotNull EntityProjectile projectile,
            @NotNull Pos position,
            @NotNull LivingEntity target
    ) {
        super(projectile, position);
        this.target = target;
    }

    public @NotNull LivingEntity getTarget() {
        return target;
    }
}
