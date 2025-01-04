package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

/**
 * Represents the result of a physics update
 */
@ApiStatus.Experimental
public sealed interface PhysicsResult permits PhysicsResultCached, PhysicsResultImpl, PhysicsResultZero {
    /**
     * @return newPosition the new position of the entity
     */
    Pos newPosition();

    /**
     * @return newVelocity the new velocity of the entity
     */
    Vec newVelocity();

    /**
     * @return if the entity is on the ground
     */
    boolean isOnGround();

    /**
     * @return if the entity collided on the X axis
     */
    boolean collisionX();

    /**
     * @return if the entity collided on the Y axis
     */
    boolean collisionY();

    /**
     * @return if the entity collided on the Z axis
     */
    boolean collisionZ();

    /**
     * @return originalDelta the velocity delta of the entity
     */
    Vec originalDelta();

    /**
     * @return collisionPoints the points where the entity collided
     */
    Point[] collisionPoints();

    /**
     * @return collisionShapes the shapes the entity collided with
     */
    Shape[] collisionShapes();

    /**
     * @return collisionShapePositions the positions of the shapes the entity collided with
     */
    Point[] collisionShapePositions();

    /**
     * @return hasCollision if the entity has collided
     */
    boolean hasCollision();

    /**
     * @return sweepResult the sweep result of the entity
     */
    SweepResult sweepResult();

    @Contract(pure = true)
    default PhysicsResultCached asCached() {
        return new PhysicsResultCached(newPosition(), newVelocity(), isOnGround(), collisionX(), collisionY(), collisionZ(), originalDelta(), collisionPoints(), collisionShapes(), collisionShapePositions(), hasCollision(), sweepResult());
    }
}
