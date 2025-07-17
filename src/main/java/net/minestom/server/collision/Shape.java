package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.block.BlockFace;
import org.jetbrains.annotations.NotNull;

public interface Shape {
    boolean isOccluded(@NotNull Shape shape, @NotNull BlockFace face);

    /**
     * Returns true if the given block face is completely covered by this shape, false otherwise.
     * @param face The face to test
     */
    default boolean isFaceFull(@NotNull BlockFace face) {
        return false;
    }

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
     * Used to know if this {@link BoundingBox} intersects with the bounding box of an entity.
     *
     * @param entity the entity to check the bounding box
     * @return true if this bounding box intersects with the entity, false otherwise
     */
    default boolean intersectEntity(@NotNull Point src, @NotNull Entity entity) {
        return intersectBox(src.sub(entity.getPosition()), entity.getBoundingBox());
    }

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
}
