package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * See https://wiki.vg/Entity_metadata#Mobs_2
 */
public class BoundingBox {

    private final Entity entity;
    private final double x, y, z;

    private final CachedFace bottomFace = new CachedFace(() -> List.of(
            new Vec(getMinX(), getMinY(), getMinZ()),
            new Vec(getMaxX(), getMinY(), getMinZ()),
            new Vec(getMaxX(), getMinY(), getMaxZ()),
            new Vec(getMinX(), getMinY(), getMaxZ())
    ));
    private final CachedFace topFace = new CachedFace(() -> List.of(
            new Vec(getMinX(), getMaxY(), getMinZ()),
            new Vec(getMaxX(), getMaxY(), getMinZ()),
            new Vec(getMaxX(), getMaxY(), getMaxZ()),
            new Vec(getMinX(), getMaxY(), getMaxZ())
    ));
    private final CachedFace leftFace = new CachedFace(() -> List.of(
            new Vec(getMinX(), getMinY(), getMinZ()),
            new Vec(getMinX(), getMaxY(), getMinZ()),
            new Vec(getMinX(), getMaxY(), getMaxZ()),
            new Vec(getMinX(), getMinY(), getMaxZ())
    ));
    private final CachedFace rightFace = new CachedFace(() -> List.of(
            new Vec(getMaxX(), getMinY(), getMinZ()),
            new Vec(getMaxX(), getMaxY(), getMinZ()),
            new Vec(getMaxX(), getMaxY(), getMaxZ()),
            new Vec(getMaxX(), getMinY(), getMaxZ())
    ));
    private final CachedFace frontFace = new CachedFace(() -> List.of(
            new Vec(getMinX(), getMinY(), getMinZ()),
            new Vec(getMaxX(), getMinY(), getMinZ()),
            new Vec(getMaxX(), getMaxY(), getMinZ()),
            new Vec(getMinX(), getMaxY(), getMinZ())
    ));
    private final CachedFace backFace = new CachedFace(() -> List.of(
            new Vec(getMinX(), getMinY(), getMaxZ()),
            new Vec(getMaxX(), getMinY(), getMaxZ()),
            new Vec(getMaxX(), getMaxY(), getMaxZ()),
            new Vec(getMinX(), getMaxY(), getMaxZ())
    ));

    /**
     * Creates a {@link BoundingBox} linked to an {@link Entity} and with a specific size.
     *
     * @param entity the linked entity
     * @param x      the width size
     * @param y      the height size
     * @param z      the depth size
     */
    public BoundingBox(@NotNull Entity entity, double x, double y, double z) {
        this.entity = entity;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Used to know if two {@link BoundingBox} intersect with each other.
     *
     * @param boundingBox the {@link BoundingBox} to check
     * @return true if the two {@link BoundingBox} intersect with each other, false otherwise
     */
    public boolean intersect(@NotNull BoundingBox boundingBox) {
        return (getMinX() <= boundingBox.getMaxX() && getMaxX() >= boundingBox.getMinX()) &&
                (getMinY() <= boundingBox.getMaxY() && getMaxY() >= boundingBox.getMinY()) &&
                (getMinZ() <= boundingBox.getMaxZ() && getMaxZ() >= boundingBox.getMinZ());
    }

    /**
     * Used to know if this {@link BoundingBox} intersects with the bounding box of an entity.
     *
     * @param entity the entity to check the bounding box
     * @return true if this bounding box intersects with the entity, false otherwise
     */
    public boolean intersect(@NotNull Entity entity) {
        return intersect(entity.getBoundingBox());
    }

    /**
     * Used to know if the bounding box intersects at a block position.
     *
     * @param blockX the block X
     * @param blockY the block Y
     * @param blockZ the block Z
     * @return true if the bounding box intersects with the position, false otherwise
     */
    public boolean intersectWithBlock(int blockX, int blockY, int blockZ) {
        final double offsetX = 1;
        final double maxX = (double) blockX + offsetX;
        final boolean checkX = getMinX() < maxX && getMaxX() > (double) blockX;
        if (!checkX) return false;

        final double maxY = (double) blockY + 0.99999;
        final boolean checkY = getMinY() < maxY && getMaxY() > (double) blockY;
        if (!checkY) return false;

        final double offsetZ = 1;
        final double maxZ = (double) blockZ + offsetZ;
        // Z check
        return getMinZ() < maxZ && getMaxZ() > (double) blockZ;
    }

    /**
     * Used to know if the bounding box intersects at a point.
     *
     * @param blockPosition the position to check
     * @return true if the bounding box intersects with the position, false otherwise
     */
    public boolean intersectWithBlock(@NotNull Point blockPosition) {
        return intersectWithBlock(blockPosition.blockX(), blockPosition.blockY(), blockPosition.blockZ());
    }

    /**
     * Used to know if the bounding box intersects (contains) a point.
     *
     * @param x x-coord of a point
     * @param y y-coord of a point
     * @param z z-coord of a point
     * @return true if the bounding box intersects (contains) with the point, false otherwise
     */
    public boolean intersect(double x, double y, double z) {
        return (x >= getMinX() && x <= getMaxX()) &&
                (y >= getMinY() && y <= getMaxY()) &&
                (z >= getMinZ() && z <= getMaxZ());
    }

    /**
     * Used to know if the bounding box intersects (contains) a point.
     *
     * @param point the point to check
     * @return true if the bounding box intersects (contains) with the point, false otherwise
     */
    public boolean intersect(@NotNull Point point) {
        return intersect(point.x(), point.y(), point.z());
    }

    /**
     * Used to know if the bounding box intersects a line segment.
     *
     * @param x1 x-coord of first line segment point
     * @param y1 y-coord of first line segment point
     * @param z1 z-coord of first line segment point
     * @param x2 x-coord of second line segment point
     * @param y2 y-coord of second line segment point
     * @param z2 z-coord of second line segment point
     * @return true if the bounding box intersects with the line segment, false otherwise.
     */
    public boolean intersect(double x1, double y1, double z1, double x2, double y2, double z2) {
        // originally from http://www.3dkingdoms.com/weekly/weekly.php?a=3
        double x3 = getMinX();
        double x4 = getMaxX();
        double y3 = getMinY();
        double y4 = getMaxY();
        double z3 = getMinZ();
        double z4 = getMaxZ();
        if (x1 > x3 && x1 < x4 && y1 > y3 && y1 < y4 && z1 > z3 && z1 < z4) {
            return true;
        }
        if (x1 < x3 && x2 < x3 || x1 > x4 && x2 > x4 ||
                y1 < y3 && y2 < y3 || y1 > y4 && y2 > y4 ||
                z1 < z3 && z2 < z3 || z1 > z4 && z2 > z4) {
            return false;
        }
        return isInsideBoxWithAxis(Axis.X, getSegmentIntersection(x1 - x3, x2 - x3, x1, y1, z1, x2, y2, z2)) ||
                isInsideBoxWithAxis(Axis.X, getSegmentIntersection(x1 - x4, x2 - x4, x1, y1, z1, x2, y2, z2)) ||
                isInsideBoxWithAxis(Axis.Y, getSegmentIntersection(y1 - y3, y2 - y3, x1, y1, z1, x2, y2, z2)) ||
                isInsideBoxWithAxis(Axis.Y, getSegmentIntersection(y1 - y4, y2 - y4, x1, y1, z1, x2, y2, z2)) ||
                isInsideBoxWithAxis(Axis.Z, getSegmentIntersection(z1 - z3, z2 - z3, x1, y1, z1, x2, y2, z2)) ||
                isInsideBoxWithAxis(Axis.Z, getSegmentIntersection(z1 - z4, z2 - z4, x1, y1, z1, x2, y2, z2));
    }

    /**
     * Used to know if the bounding box intersects a line segment.
     *
     * @param start first line segment point
     * @param end   second line segment point
     * @return true if the bounding box intersects with the line segment, false otherwise.
     */
    public boolean intersect(@NotNull Point start, @NotNull Point end) {
        return intersect(
                Math.min(start.x(), end.x()),
                Math.min(start.y(), end.y()),
                Math.min(start.z(), end.z()),
                Math.max(start.x(), end.x()),
                Math.max(start.y(), end.y()),
                Math.max(start.z(), end.z())
        );
    }

    private @Nullable Vec getSegmentIntersection(double dst1, double dst2, double x1, double y1, double z1, double x2, double y2, double z2) {
        if (dst1 == dst2 || dst1 * dst2 >= 0D) return null;
        final double delta = dst1 / (dst1 - dst2);
        return new Vec(
                x1 + (x2 - x1) * delta,
                y1 + (y2 - y1) * delta,
                z1 + (z2 - z1) * delta
        );
    }

    private boolean isInsideBoxWithAxis(Axis axis, @Nullable Vec intersection) {
        if (intersection == null) return false;
        double x1 = getMinX();
        double x2 = getMaxX();
        double y1 = getMinY();
        double y2 = getMaxY();
        double z1 = getMinZ();
        double z2 = getMaxZ();
        return axis == Axis.X && intersection.z() > z1 && intersection.z() < z2 && intersection.y() > y1 && intersection.y() < y2 ||
                axis == Axis.Y && intersection.z() > z1 && intersection.z() < z2 && intersection.x() > x1 && intersection.x() < x2 ||
                axis == Axis.Z && intersection.x() > x1 && intersection.x() < x2 && intersection.y() > y1 && intersection.y() < y2;
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
        return new BoundingBox(entity, this.x + x, this.y + y, this.z + z);
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
        return new BoundingBox(entity, this.x - x, this.y - y, this.z - z);
    }

    /**
     * Gets the width of the {@link BoundingBox}.
     *
     * @return the width
     */
    public double getWidth() {
        return x;
    }

    /**
     * Gets the height of the {@link BoundingBox}.
     *
     * @return the height
     */
    public double getHeight() {
        return y;
    }

    /**
     * Gets the depth of the {@link BoundingBox}.
     *
     * @return the depth
     */
    public double getDepth() {
        return z;
    }

    /**
     * Gets the min X based on {@link #getWidth()} and the {@link Entity} position.
     *
     * @return the min X
     */
    public double getMinX() {
        return entity.getPosition().x() - (x / 2);
    }

    /**
     * Gets the max X based on {@link #getWidth()} and the {@link Entity} position.
     *
     * @return the max X
     */
    public double getMaxX() {
        return entity.getPosition().x() + (x / 2);
    }

    /**
     * Gets the min Y based on the {@link Entity} position.
     *
     * @return the min Y
     */
    public double getMinY() {
        return entity.getPosition().y();
    }

    /**
     * Gets the max Y based on {@link #getHeight()} and the {@link Entity} position.
     *
     * @return the max Y
     */
    public double getMaxY() {
        return entity.getPosition().y() + y;
    }

    /**
     * Gets the min Z based on {@link #getDepth()} and the {@link Entity} position.
     *
     * @return the min Z
     */
    public double getMinZ() {
        return entity.getPosition().z() - (z / 2);
    }

    /**
     * Gets the max Z based on {@link #getDepth()} and the {@link Entity} position.
     *
     * @return the max Z
     */
    public double getMaxZ() {
        return entity.getPosition().z() + (z / 2);
    }

    /**
     * Gets an array of {@link Vec} representing the points at the bottom of the {@link BoundingBox}.
     *
     * @return the points at the bottom of the {@link BoundingBox}
     */
    public @NotNull List<Vec> getBottomFace() {
        return bottomFace.get();
    }

    /**
     * Gets an array of {@link Vec} representing the points at the top of the {@link BoundingBox}.
     *
     * @return the points at the top of the {@link BoundingBox}
     */
    public @NotNull List<Vec> getTopFace() {
        return topFace.get();
    }

    /**
     * Gets an array of {@link Vec} representing the points on the left face of the {@link BoundingBox}.
     *
     * @return the points on the left face of the {@link BoundingBox}
     */
    public @NotNull List<Vec> getLeftFace() {
        return leftFace.get();
    }

    /**
     * Gets an array of {@link Vec} representing the points on the right face of the {@link BoundingBox}.
     *
     * @return the points on the right face of the {@link BoundingBox}
     */
    public @NotNull List<Vec> getRightFace() {
        return rightFace.get();
    }

    /**
     * Gets an array of {@link Vec} representing the points at the front of the {@link BoundingBox}.
     *
     * @return the points at the front of the {@link BoundingBox}
     */
    public @NotNull List<Vec> getFrontFace() {
        return frontFace.get();
    }

    /**
     * Gets an array of {@link Vec} representing the points at the back of the {@link BoundingBox}.
     *
     * @return the points at the back of the {@link BoundingBox}
     */
    public @NotNull List<Vec> getBackFace() {
        return backFace.get();
    }

    @Override
    public String toString() {
        String result = "BoundingBox";
        result += "\n";
        result += "[" + getMinX() + " : " + getMaxX() + "]";
        result += "\n";
        result += "[" + getMinY() + " : " + getMaxY() + "]";
        result += "\n";
        result += "[" + getMinZ() + " : " + getMaxZ() + "]";
        return result;
    }

    private enum Axis {
        X, Y, Z
    }

    private final class CachedFace {
        private final AtomicReference<@Nullable PositionedPoints> reference = new AtomicReference<>(null);
        private final Supplier<@NotNull List<Vec>> faceProducer;

        private CachedFace(Supplier<@NotNull List<Vec>> faceProducer) {
            this.faceProducer = faceProducer;
        }

        @NotNull List<Vec> get() {
            //noinspection ConstantConditions
            return reference.updateAndGet(value -> {
                Pos entityPosition = entity.getPosition();
                if (value == null || !value.lastPosition.samePoint(entityPosition)) {
                    return new PositionedPoints(entityPosition, faceProducer.get());
                }
                return value;
            }).points;
        }
    }

    private static final class PositionedPoints {
        private final @NotNull Pos lastPosition;
        private final @NotNull List<Vec> points;

        private PositionedPoints(@NotNull Pos lastPosition, @NotNull List<Vec> points) {
            this.lastPosition = lastPosition;
            this.points = points;
        }
    }
}
