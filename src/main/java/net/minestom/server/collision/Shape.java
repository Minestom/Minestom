package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.BlockFace;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Experimental
public interface Shape {
    /**
     * Checks if two bounding boxes intersect.
     *
     * @param positionRelative Relative position of bounding box to check with
     * @param boundingBox      Bounding box to check for intersections with
     * @return is an intersection found
     */
    boolean intersectBox(@NotNull Point positionRelative, @NotNull BoundingBox boundingBox);

    /**
     * Checks if a moving bounding box will hit this shape.
     *
     * @param rayStart     Position of the moving shape
     * @param rayDirection Movement vector
     * @param shapePos     Position of this shape
     * @param moving       Bounding Box of moving shape
     * @param finalResult  Stores final SweepResult
     * @return is an intersection found
     */
    boolean intersectBoxSwept(@NotNull Point rayStart, @NotNull Point rayDirection,
                              @NotNull Point shapePos, @NotNull BoundingBox moving, @NotNull SweepResult finalResult);

    /**
     * Relative Start
     *
     * @return Start of shape
     */
    @NotNull Point relativeStart();

    /**
     * Relative End
     *
     * @return End of shape
     */
    @NotNull Point relativeEnd();

    /**
     * Check if occlusion block face is full
     * @param face Face to check
     * @return true if face is full
     */
    boolean isOccluded(BlockFace face);

    /**
     * Check if addition of two shape faces is full
     * @param shape shape to add
     * @param face face to add
     * @return true if combined face is full
     */
    boolean isAdditionOccluded(Shape shape, BlockFace face);
}
