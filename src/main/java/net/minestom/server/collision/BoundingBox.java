package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.block.BlockFace;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * See https://wiki.vg/Entity_metadata#Mobs_2
 */
public final class BoundingBox implements Shape {

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
        return (minX() + positionRelative.x() <= boundingBox.maxX() && maxX() + positionRelative.x() >= boundingBox.minX()) &&
                (minY() + positionRelative.y() <= boundingBox.maxY() && maxY() + positionRelative.y() >= boundingBox.minY()) &&
                (minZ() + positionRelative.z() <= boundingBox.maxZ() && maxZ() + positionRelative.z() >= boundingBox.minZ());
    }

    @Override
    @ApiStatus.Experimental
    public boolean intersectBoxSwept(@NotNull Point rayStart, @NotNull Point rayDirection, @NotNull Point shapePos, @NotNull BoundingBox moving, @NotNull SweepResult finalResult) {
        final boolean isHit = RayUtils.BoundingBoxIntersectionCheck(
                moving, rayStart, rayDirection,
                this,
                shapePos
        );
        if (!isHit) return false;
        if (RayUtils.SweptAABB(moving, rayStart, rayDirection, this, shapePos, finalResult)) {
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
    public boolean isOccluded(@NotNull Shape shape, @NotNull BlockFace face) {
        return false;
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
}
