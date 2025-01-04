package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.UnknownNullability;

@ApiStatus.Experimental
public record PhysicsResultImpl(
        Pos newPosition,
        Vec newVelocity,
        boolean isOnGround,
        boolean collisionX,
        boolean collisionY,
        boolean collisionZ,
        Vec originalDelta,
        @UnknownNullability Point @UnknownNullability [] collisionPoints,
        @UnknownNullability Shape @UnknownNullability [] collisionShapes,
        @UnknownNullability Point @UnknownNullability [] collisionShapePositions,
        boolean hasCollision,
        SweepResult sweepResult
) implements PhysicsResult {
}

