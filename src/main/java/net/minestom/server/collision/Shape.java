package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Experimental
public interface Shape {
    /**
     * Check if two bounding boxes intersect
     * @param positionRelative Relative position of bounding box to check with
     * @param boundingBox Bounding box to check for intersections with
     * @return is an intersection found
     */
    boolean intersectBox(Point positionRelative, BoundingBox boundingBox);

    /**
     * Check if a moving shape will hit this shape
     * @param rayStart Position of the moving shape
     * @param rayDirection Movement vector
     * @param blockPos Position of this shape
     * @param moving Bounding Box of moving shape
     * @param tempResult Stores temporary SweepResult
     * @param finalResult Stores final SweepResult
     * @return is an intersection found
     */
    boolean intersectBoxSwept(Point rayStart, Point rayDirection, Point blockPos, BoundingBox moving, SweepResult tempResult, SweepResult finalResult);

    /**
     * Relative Start
     * @return Start of shape
     */
    @NotNull Point relativeStart();

    /**
     * Relative End
     * @return End of shape
     */
    @NotNull Point relativeEnd();
}
