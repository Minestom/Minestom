package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the result of a collision with an entity
 * @param collisionPoint
 * @param entity
 * @param face null if the collision is not with a face
 */
public record EntityCollisionResult(
        @NotNull Point collisionPoint,
        @NotNull Entity entity,
        @Nullable BlockFace face,
        double percentage
) implements Comparable<EntityCollisionResult> {
    @Override
    public int compareTo(@NotNull EntityCollisionResult o) {
        return Double.compare(percentage, o.percentage);
    }
}
