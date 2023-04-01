package net.minestom.server.event.entity.projectile;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

public final class ProjectileCollideWithBlockEvent extends ProjectileCollideEvent {

    private final @NotNull Point collidedPosition;
    private final @NotNull Block block;

    public ProjectileCollideWithBlockEvent(@NotNull Entity projectile, @NotNull Pos collisionPosition,
                                           @NotNull Point collidedPosition, @NotNull Block block) {
        super(projectile, collisionPosition);
        this.collidedPosition = collidedPosition;
        this.block = block;
    }

    public @NotNull Point getCollidedBlockPosition() {
        return collidedPosition;
    }

    public @NotNull Block getBlock() {
        return block;
    }
}
