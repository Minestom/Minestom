package net.minestom.server.entity.pathfinding.followers;

import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.coordinate.Point;
import org.jspecify.annotations.Nullable;

public interface NodeFollower {
    /**
     * Move towards the specified point
     *
     * @param target the point to move towards
     * @param speed  the speed to move at
     * @param lookAt the point to look at
     */
    void moveTowards(Point target, double speed, Point lookAt);

    /**
     * Jump
     */
    void jump(@Nullable Point point, @Nullable Point target);

    /**
     * Check if the follower is at the specified point
     * @param point the point to check
     * @return true if the follower is at the point
     */
    boolean isAtPoint(Point point);

    /**
     * Get the movement speed of the follower
     * @return the movement speed
     */
    double movementSpeed();
}