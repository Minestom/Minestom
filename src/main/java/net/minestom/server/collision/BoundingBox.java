package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * See https://wiki.vg/Entity_metadata#Mobs_2
 */
public class BoundingBox {

    private final Entity entity;
    private final double x, y, z;

    private volatile Pos lastPosition;
    private List<Vec> bottomFace;
    private List<Vec> topFace;
    private List<Vec> leftFace;
    private List<Vec> rightFace;
    private List<Vec> frontFace;
    private List<Vec> backFace;

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
        if (!checkX)
            return false;

        final double maxY = (double) blockY + 0.99999;

        final boolean checkY = getMinY() < maxY && getMaxY() > (double) blockY;
        if (!checkY)
            return false;

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

    public boolean intersect(double x, double y, double z) {
        return (x >= getMinX() && x <= getMaxX()) &&
                (y >= getMinY() && y <= getMaxY()) &&
                (z >= getMinZ() && z <= getMaxZ());
    }

    public boolean intersect(@NotNull Point point) {
        return intersect(point.x(), point.y(), point.z());
    }

    /**
     * Creates a new {@link BoundingBox} linked to the same {@link Entity} with expanded size.
     *
     * @param x the X offset
     * @param y the Y offset
     * @param z the Z offset
     * @return a new {@link BoundingBox} expanded
     */
    @NotNull
    public BoundingBox expand(double x, double y, double z) {
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
    @NotNull
    public BoundingBox contract(double x, double y, double z) {
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
        this.bottomFace = get(bottomFace, () ->
                List.of(new Vec(getMinX(), getMinY(), getMinZ()),
                        new Vec(getMaxX(), getMinY(), getMinZ()),
                        new Vec(getMaxX(), getMinY(), getMaxZ()),
                        new Vec(getMinX(), getMinY(), getMaxZ())));
        return bottomFace;
    }

    /**
     * Gets an array of {@link Vec} representing the points at the top of the {@link BoundingBox}.
     *
     * @return the points at the top of the {@link BoundingBox}
     */
    public @NotNull List<Vec> getTopFace() {
        this.topFace = get(topFace, () ->
                List.of(new Vec(getMinX(), getMaxY(), getMinZ()),
                        new Vec(getMaxX(), getMaxY(), getMinZ()),
                        new Vec(getMaxX(), getMaxY(), getMaxZ()),
                        new Vec(getMinX(), getMaxY(), getMaxZ())));
        return topFace;
    }

    /**
     * Gets an array of {@link Vec} representing the points on the left face of the {@link BoundingBox}.
     *
     * @return the points on the left face of the {@link BoundingBox}
     */
    public @NotNull List<Vec> getLeftFace() {
        this.leftFace = get(leftFace, () ->
                List.of(new Vec(getMinX(), getMinY(), getMinZ()),
                        new Vec(getMinX(), getMaxY(), getMinZ()),
                        new Vec(getMinX(), getMaxY(), getMaxZ()),
                        new Vec(getMinX(), getMinY(), getMaxZ())));
        return leftFace;
    }

    /**
     * Gets an array of {@link Vec} representing the points on the right face of the {@link BoundingBox}.
     *
     * @return the points on the right face of the {@link BoundingBox}
     */
    public @NotNull List<Vec> getRightFace() {
        this.rightFace = get(rightFace, () ->
                List.of(new Vec(getMaxX(), getMinY(), getMinZ()),
                        new Vec(getMaxX(), getMaxY(), getMinZ()),
                        new Vec(getMaxX(), getMaxY(), getMaxZ()),
                        new Vec(getMaxX(), getMinY(), getMaxZ())));
        return rightFace;
    }

    /**
     * Gets an array of {@link Vec} representing the points at the front of the {@link BoundingBox}.
     *
     * @return the points at the front of the {@link BoundingBox}
     */
    public @NotNull List<Vec> getFrontFace() {
        this.frontFace = get(frontFace, () ->
                List.of(new Vec(getMinX(), getMinY(), getMinZ()),
                        new Vec(getMaxX(), getMinY(), getMinZ()),
                        new Vec(getMaxX(), getMaxY(), getMinZ()),
                        new Vec(getMinX(), getMaxY(), getMinZ())));
        return frontFace;
    }

    /**
     * Gets an array of {@link Vec} representing the points at the back of the {@link BoundingBox}.
     *
     * @return the points at the back of the {@link BoundingBox}
     */
    public @NotNull List<Vec> getBackFace() {
        this.backFace = get(backFace, () -> List.of(
                new Vec(getMinX(), getMinY(), getMaxZ()),
                new Vec(getMaxX(), getMinY(), getMaxZ()),
                new Vec(getMaxX(), getMaxY(), getMaxZ()),
                new Vec(getMinX(), getMaxY(), getMaxZ())));
        return backFace;
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

    private @NotNull List<Vec> get(@Nullable List<Vec> face, Supplier<? extends List<Vec>> vecSupplier) {
        final var lastPos = this.lastPosition;
        final var entityPos = entity.getPosition();
        if (face != null && lastPos != null && lastPos.samePoint(entityPos)) {
            return face;
        }
        this.lastPosition = entityPos;
        return vecSupplier.get();
    }
}
