package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the result of a collision with an entity
 * @param collisionPoint
 * @param entity
 * @param direction the direction of the collision. ex. Vec(-1, 0, 0) means the entity collided with the west face of the entity
 */
public record EntityCollisionResult(
        @NotNull Point collisionPoint,
        @NotNull Entity entity,
        @NotNull Vec direction,
        double percentage
) implements Comparable<EntityCollisionResult> {
    @Override
    public int compareTo(@NotNull EntityCollisionResult o) {
        return Double.compare(percentage, o.percentage);
    }
}
