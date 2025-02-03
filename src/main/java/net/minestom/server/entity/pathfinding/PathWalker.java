package net.minestom.server.entity.pathfinding;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents something that can follow a generated path from {@link PathGenerator}
 */
public interface PathWalker {

    @NotNull
    BoundingBox getBoundingBox();

    @Nullable
    Instance getInstance();

    @NotNull
    Pos getPosition();

    boolean isOnGround();

    void updateNewPosition(@NotNull Vec speed, float yaw, float pitch);

    void setVelocity(@NotNull Vec newVelocity);
}
