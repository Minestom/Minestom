package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * See https://wiki.vg/Entity_metadata#Mobs_2
 */
public final class BoundingBox implements Shape {
    private static final BoundingBox sleepingBoundingBox = new BoundingBox(0.2, 0.2, 0.2);
    private static final BoundingBox sneakingBoundingBox = new BoundingBox(0.6, 1.5, 0.6);
    private static final BoundingBox smallBoundingBox = new BoundingBox(0.6, 0.6, 0.6);

    final static BoundingBox ZERO = new BoundingBox(0, 0, 0);

    private final double width, height, depth;
    private final Point offset;
    private Point relativeEnd;

    BoundingBox(double width, double height, double depth, Point offset) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.offset = offset;
    }

    public BoundingBox(double width, double height, double depth) {
        this(width, height, depth, new Vec(-width / 2, 0, -depth / 2));
    }

    @Override
    @ApiStatus.Experimental
    public boolean intersectBox(@NotNull Point positionRelative, @NotNull BoundingBox boundingBox) {
        return (minX() + positionRelative.x() <= boundingBox.maxX() - Vec.EPSILON / 2 && maxX() + positionRelative.x() >= boundingBox.minX() + Vec.EPSILON / 2) &&
                (minY() + positionRelative.y() <= boundingBox.maxY() - Vec.EPSILON / 2 && maxY() + positionRelative.y() >= boundingBox.minY() + Vec.EPSILON / 2) &&
                (minZ() + positionRelative.z() <= boundingBox.maxZ() - Vec.EPSILON / 2 && maxZ() + positionRelative.z() >= boundingBox.minZ() + Vec.EPSILON / 2);
    }

    @Override
    @ApiStatus.Experimental
    public boolean intersectBoxSwept(@NotNull Point rayStart, @NotNull Point rayDirection, @NotNull Point shapePos, @NotNull BoundingBox moving, @NotNull SweepResult finalResult) {
        if (RayUtils.BoundingBoxIntersectionCheck(moving, rayStart, rayDirection, this, shapePos, finalResult) ) {
            finalResult.collidedShapePosition = shapePos;
            finalResult.collidedShape = this;
            finalResult.blockType = null;
        }
        return true;
    }

    /**
     * Used to know if this {@link BoundingBox} intersects with the bounding box of an entity.
     *
     * @param entity the entity to check the bounding box
     * @return true if this bounding box intersects with the entity, false otherwise
     */
    @ApiStatus.Experimental
    public boolean intersectEntity(@NotNull Point src, @NotNull Entity entity) {
        return intersectBox(src.sub(entity.getPosition()), entity.getBoundingBox());
    }

    @ApiStatus.Experimental
    public boolean boundingBoxRayIntersectionCheck(Vec start, Vec direction, Pos position) {
        return RayUtils.BoundingBoxRayIntersectionCheck(start, direction, this, position);
    }

    @Override
    public @NotNull Point relativeStart() {
        return offset;
    }

    @Override
    public @NotNull Point relativeEnd() {
        Point relativeEnd = this.relativeEnd;
        if (relativeEnd == null) this.relativeEnd = relativeEnd = offset.add(width, height, depth);
        return relativeEnd;
    }

    @Override
    public String toString() {
        String result = "BoundingBox";
        result += "\n";
        result += "[" + minX() + " : " + maxX() + "]";
        result += "\n";
        result += "[" + minY() + " : " + maxY() + "]";
        result += "\n";
        result += "[" + minZ() + " : " + maxZ() + "]";
        return result;
    }

    /**
     * Creates a new {@link BoundingBox} linked to the same {@link Entity} with expanded size.
     *
     * @param x the X offset
     * @param y the Y offset
     * @param z the Z offset
     * @return a new {@link BoundingBox} expanded
     */
    public @NotNull BoundingBox expand(double x, double y, double z) {
        return new BoundingBox(this.width + x, this.height + y, this.depth + z);
    }

    /**
     * Creates a new {@link BoundingBox} linked to the same {@link Entity} with contracted size.
     *
     * @param x the X offset
     * @param y the Y offset
     * @param z the Z offset
     * @return a new bounding box contracted
     */
    public @NotNull BoundingBox contract(double x, double y, double z) {
        return new BoundingBox(this.width - x, this.height - y, this.depth - z);
    }

    public double width() {
        return width;
    }

    public double height() {
        return height;
    }

    public double depth() {
        return depth;
    }

    public double minX() {
        return relativeStart().x();
    }

    public double maxX() {
        return relativeEnd().x();
    }

    public double minY() {
        return relativeStart().y();
    }

    public double maxY() {
        return relativeEnd().y();
    }

    public double minZ() {
        return relativeStart().z();
    }

    public double maxZ() {
        return relativeEnd().z();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoundingBox that = (BoundingBox) o;
        if (Double.compare(that.width, width) != 0) return false;
        if (Double.compare(that.height, height) != 0) return false;
        if (Double.compare(that.depth, depth) != 0) return false;
        return offset.equals(that.offset);
    }

    public static @Nullable BoundingBox fromPose(@NotNull Entity.Pose pose) {
        return switch (pose) {
            case FALL_FLYING, SWIMMING, SPIN_ATTACK -> smallBoundingBox;
            case SLEEPING, DYING -> sleepingBoundingBox;
            case SNEAKING -> sneakingBoundingBox;
            default -> null;
        };
    }
}
