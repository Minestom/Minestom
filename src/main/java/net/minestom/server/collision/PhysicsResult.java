package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnknownNullability;

/**
 * The result of a physics simulation.
 * @param newPosition the new position of the entity
 * @param newVelocity the new velocity of the entity
 * @param isOnGround if the entity is on the ground
 * @param collisionX if the entity collided on the X axis
 * @param collisionY if the entity collided on the Y axis
 * @param collisionZ if the entity collided on the Z axis
 * @param originalDelta the velocity delta of the entity
 * @param collisionPoints the points where the entity collided
 * @param collisionShapes the shapes the entity collided with
 * @param collisionShapePositions the positions of the shapes the entity collided with
 */
@ApiStatus.Experimental
public record PhysicsResult(
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
        SweepResult res
) {
}
