package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.block.BlockFace;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

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

    public BoundingBox(double width, double height, double depth, Point offset) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.offset = offset;
    }

    public BoundingBox(double width, double height, double depth) {
        this(width, height, depth, new Vec(-width / 2, 0, -depth / 2));
    }

    @Override
    public boolean isOccluded(@NotNull Shape shape, @NotNull BlockFace face) {
        return false;
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
            finalResult.collidedPositionX = rayStart.x() + rayDirection.x() * finalResult.res;
            finalResult.collidedPositionY = rayStart.y() + rayDirection.y() * finalResult.res;
            finalResult.collidedPositionZ = rayStart.z() + rayDirection.z() * finalResult.res;
            finalResult.collidedShape = this;
            return true;
        }

        return false;
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
     * Creates a new {@link BoundingBox} with an expanded size.
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
     * Creates a new {@link BoundingBox} with a contracted size.
     *
     * @param x the X offset
     * @param y the Y offset
     * @param z the Z offset
     * @return a new bounding box contracted
     */
    public @NotNull BoundingBox contract(double x, double y, double z) {
        return new BoundingBox(this.width - x, this.height - y, this.depth - z);
    }

    /**
     * Creates a new {@link BoundingBox} with an offset.
     *
     * @param offset the offset
     * @return a new bounding box with an offset.
     */
    public @NotNull BoundingBox withOffset(Point offset) {
        return new BoundingBox(this.width, this.height, this.depth, offset);
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

    public enum AxisMask {
        X,
        Y,
        Z,
        NONE
    }

    public PointIterator getBlocks(Point point) {
        return new PointIterator(this, point, AxisMask.NONE, 0);
    }

    public PointIterator getBlocks(Point point, AxisMask axisMask, double axis) {
        return new PointIterator(this, point, axisMask, axis);
    }

    public static class MutablePoint {
        double x, y, z;

        public void set(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public double x() {
            return x;
        }

        public double y() {
            return y;
        }

        public double z() {
            return z;
        }

        public int blockX() {
            return (int) Math.floor(x);
        }

        public int blockY() {
            return (int) Math.floor(y);
        }

        public int blockZ() {
            return (int) Math.floor(z);
        }
    }

    public static class PointIterator implements Iterator<MutablePoint> {
        private double sx, sy, sz;
        double x, y, z;
        private double minX, minY, minZ, maxX, maxY, maxZ;
        private final MutablePoint point = new MutablePoint();

        public PointIterator() {}
        public PointIterator(BoundingBox boundingBox, Point p, AxisMask axisMask, double axis) {
            reset(boundingBox, p, axisMask, axis);
        }

        public void reset(BoundingBox boundingBox, double pointX, double pointY, double pointZ, AxisMask axisMask, int axis) {
            minX = (int) Math.floor(boundingBox.minX() + pointX);
            minY = (int) Math.floor(boundingBox.minY() + pointY);
            minZ = (int) Math.floor(boundingBox.minZ() + pointZ);
            maxX = (int) Math.floor(boundingBox.maxX() + pointX);
            maxY = (int) Math.floor(boundingBox.maxY() + pointY);
            maxZ = (int) Math.floor(boundingBox.maxZ() + pointZ);

            x = minX;
            y = minY;
            z = minZ;

            sx = boundingBox.minX() + pointX - minX;
            sy = boundingBox.minY() + pointY - minY;
            sz = boundingBox.minZ() + pointZ - minZ;

            if (axisMask == AxisMask.X) {
                x = axis + pointX;
                minX = x;
                maxX = x;
            } else if (axisMask == AxisMask.Y) {
                y = axis + pointY;
                minY = y;
                maxY = y;
            } else if (axisMask == AxisMask.Z) {
                z = axis + pointZ;
                minZ = z;
                maxZ = z;
            }
        }

        public void reset(BoundingBox boundingBox, Point p, AxisMask axisMask, double axis) {
            reset(boundingBox, p.x(), p.y(), p.z(), axisMask, (int) axis);
        }

        public void reset(BoundingBox boundingBox, double x, double y, double z, AxisMask axisMask, double axis) {
            reset(boundingBox, x, y, z, axisMask, (int) axis);
        }

        @Override
        public boolean hasNext() {
            return x <= maxX && y <= maxY && z <= maxZ;
        }

        @Override
        public MutablePoint next() {
            point.set(x + sx, y + sy, z + sz);

            x++;
            if (x > maxX) {
                x = minX;
                y++;
                if (y > maxY) {
                    y = minY;
                    z++;
                }
            }
            return point;
        }
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

    public static @NotNull BoundingBox fromPoints(@NotNull Point a, @NotNull Point b) {
        Vec aVec = Vec.fromPoint(a);
        Vec min = aVec.min(b);
        Vec max = aVec.max(b);
        Vec dimensions = max.sub(min);
        return new BoundingBox(dimensions.x(), dimensions.y(), dimensions.z(), min);
    }
}
