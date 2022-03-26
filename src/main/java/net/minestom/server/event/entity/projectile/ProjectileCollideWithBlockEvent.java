package net.minestom.server.event.entity.projectile;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

public final class ProjectileCollideWithBlockEvent extends ProjectileCollideEvent {

    private final @NotNull Block block;

    public ProjectileCollideWithBlockEvent(
            @NotNull EntityProjectile projectile,
            @NotNull Pos position,
            @NotNull Block block
    ) {
        super(projectile, position);
        this.block = block;
    }

    public @NotNull Block getBlock() {
        return block;
    }
}
