package net.minestom.server.entity.pathfinding;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public interface Navigator {
    default boolean setPathTo(@Nullable Point point) {
        BoundingBox bb = getEntity().getBoundingBox();
        double centerToCorner = Math.sqrt(bb.width() * bb.width() + bb.depth() * bb.depth()) / 2;
        return setPathTo(point, centerToCorner, null);
    }

    default boolean setPathTo(@Nullable Point point, double reachRange, @Nullable Runnable onComplete) {
        return setPathTo(point, reachRange, 50, 20, onComplete);
    }

    /**
     * Sets the path to {@code position} and ask the entity to follow the path.
     *
     * @param point           the position to find the path to, null to reset the pathfinder
     * @param reachRange      distance to target when completed
     * @param maxDistance     maximum search distance
     * @param pathVariance    how far to search off of the direct path.
     *                        For open worlds, this can be low (around 20) and for large mazes this needs to be very high.
     *                        Implementations are not guaranteed to take this argument into account.
     * @param onComplete      called when the path has been completed
     * @return true if a path is being generated
     */
    boolean setPathTo(@Nullable Point point, double reachRange, double maxDistance, double pathVariance, @Nullable Runnable onComplete);

    @ApiStatus.Internal
    void tick();

    void reset();

    boolean isComplete();

    /**
     * Gets the entity which is navigating.
     *
     * @return the entity
     */
    Entity getEntity();

    /**
     * Gets the target pathfinder position.
     *
     * @return the target pathfinder position, null if there is no one
     */
    @Nullable Point getTargetPosition();
}
