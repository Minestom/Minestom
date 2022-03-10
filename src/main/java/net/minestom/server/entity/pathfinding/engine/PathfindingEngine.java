package net.minestom.server.entity.pathfinding.engine;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.pathfinding.NavigableEntity;
import org.jetbrains.annotations.NotNull;

/**
 * The engine to use while pathfinding.
 *
 * Note that the methods within this class will return a {@link PathfindingResult} that represents the result of the
 * pathfinding task. This means that pathfinding may be done in a separate thread if the implementation wants to force
 * it. If {@link #async} returns false, you will need to handle threading yourself.
 */
public interface PathfindingEngine<N extends NavigableEntity<?>> {

    /**
     * Finds the path between these two static positions given a bounding box.
     *
     * @param box the bounding box
     * @param start the starting position
     * @param end the ending position
     * @return the result
     */
    @NotNull PathfindingResult findP2P(
            @NotNull BoundingBox box,
            @NotNull Point start,
            @NotNull Point end
    );

    /**
     * Finds the path between the specified navigator and the end point.
     *
     * @param start the moving navigator
     * @param end the ending position
     * @return the result
     */
    @NotNull PathfindingResult findN2P(
            @NotNull N start,
            @NotNull Point end
    );

    /**
     * Finds the path between the end point and the specified navigator.
     *
     * @param start the start position
     * @param end the navigator to end at
     * @return the result
     */
    @NotNull PathfindingResult findP2N(
            @NotNull BoundingBox box,
            @NotNull Point start,
            @NotNull N end
    );

    /**
     * Finds the path between the specified navigator.
     *
     * @param start the moving navigator
     * @param end the navigator to end at
     * @return the result
     */
    @NotNull PathfindingResult findN2N(
            @NotNull N start,
            @NotNull N end
    );

    /**
     * Returns true if minestom should handle threading before calling the methods in this class, false if this
     * pathfinding engine should handle the threading.
     * @return true if minestom should handle threading before calling the methods in this class, false otherwise.
     */
    boolean async();
}
