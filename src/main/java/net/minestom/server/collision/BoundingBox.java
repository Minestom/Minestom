package net.minestom.server.collision;

import net.minestom.server.entity.Entity;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * See https://wiki.vg/Entity_metadata#Mobs_2
 */
public class BoundingBox {

    private final Entity entity;
    private final double x, y, z;

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
     * Used to know if the bounding box intersects at a {@link BlockPosition}.
     *
     * @param blockPosition the position to check
     * @return true if the bounding box intersects with the position, false otherwise
     */
    public boolean intersect(@NotNull BlockPosition blockPosition) {

        final double offsetX = 1;
        final double x = blockPosition.getX();
        final double maxX = x + offsetX;

        final boolean checkX = getMinX() < maxX && getMaxX() > x;
        if (!checkX)
            return false;

        final double y = blockPosition.getY();
        final double maxY = y + 0.99999;

        final boolean checkY = getMinY() < maxY && getMaxY() > y;
        if (!checkY)
            return false;

        final double offsetZ = 1;
        final double z = blockPosition.getZ();
        final double maxZ = z + offsetZ;

        // Z check
        return getMinZ() < maxZ && getMaxZ() > z;
    }

    public boolean intersect(double x, double y, double z) {
        return (x >= getMinX() && x <= getMaxX()) &&
                (y >= getMinY() && y <= getMaxY()) &&
                (z >= getMinZ() && z <= getMaxZ());
    }

    public boolean intersect(@NotNull Position position) {
        return intersect(position.getX(), position.getY(), position.getZ());
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
        return entity.getPosition().getX() - (x / 2);
    }

    /**
     * Gets the max X based on {@link #getWidth()} and the {@link Entity} position.
     *
     * @return the max X
     */
    public double getMaxX() {
        return entity.getPosition().getX() + (x / 2);
    }

    /**
     * Gets the min Y based on the {@link Entity} position.
     *
     * @return the min Y
     */
    public double getMinY() {
        return entity.getPosition().getY();
    }

    /**
     * Gets the max Y based on {@link #getHeight()} and the {@link Entity} position.
     *
     * @return the max Y
     */
    public double getMaxY() {
        return entity.getPosition().getY() + y;
    }

    /**
     * Gets the min Z based on {@link #getDepth()} and the {@link Entity} position.
     *
     * @return the min Z
     */
    public double getMinZ() {
        return entity.getPosition().getZ() - (z / 2);
    }

    /**
     * Gets the max Z based on {@link #getDepth()} and the {@link Entity} position.
     *
     * @return the max Z
     */
    public double getMaxZ() {
        return entity.getPosition().getZ() + (z / 2);
    }

    /**
     * Gets an array of {@link Vector} representing the points at the bottom of the {@link BoundingBox}.
     *
     * @return the points at the bottom of the {@link BoundingBox}
     */
    @NotNull
    public Vector[] getBottomFace() {
        return new Vector[]{
                new Vector(getMinX(), getMinY(), getMinZ()),
                new Vector(getMaxX(), getMinY(), getMinZ()),
                new Vector(getMaxX(), getMinY(), getMaxZ()),
                new Vector(getMinX(), getMinY(), getMaxZ()),
        };
    }

    /**
     * Gets an array of {@link Vector} representing the points at the top of the {@link BoundingBox}.
     *
     * @return the points at the top of the {@link BoundingBox}
     */
    @NotNull
    public Vector[] getTopFace() {
        return new Vector[]{
                new Vector(getMinX(), getMaxY(), getMinZ()),
                new Vector(getMaxX(), getMaxY(), getMinZ()),
                new Vector(getMaxX(), getMaxY(), getMaxZ()),
                new Vector(getMinX(), getMaxY(), getMaxZ()),
        };
    }

    /**
     * Gets an array of {@link Vector} representing the points on the left face of the {@link BoundingBox}.
     *
     * @return the points on the left face of the {@link BoundingBox}
     */
    @NotNull
    public Vector[] getLeftFace() {
        return new Vector[]{
                new Vector(getMinX(), getMinY(), getMinZ()),
                new Vector(getMinX(), getMaxY(), getMinZ()),
                new Vector(getMinX(), getMaxY(), getMaxZ()),
                new Vector(getMinX(), getMinY(), getMaxZ()),
        };
    }

    /**
     * Gets an array of {@link Vector} representing the points on the right face of the {@link BoundingBox}.
     *
     * @return the points on the right face of the {@link BoundingBox}
     */
    @NotNull
    public Vector[] getRightFace() {
        return new Vector[]{
                new Vector(getMaxX(), getMinY(), getMinZ()),
                new Vector(getMaxX(), getMaxY(), getMinZ()),
                new Vector(getMaxX(), getMaxY(), getMaxZ()),
                new Vector(getMaxX(), getMinY(), getMaxZ()),
        };
    }

    /**
     * Gets an array of {@link Vector} representing the points at the front of the {@link BoundingBox}.
     *
     * @return the points at the front of the {@link BoundingBox}
     */
    @NotNull
    public Vector[] getFrontFace() {
        return new Vector[]{
                new Vector(getMinX(), getMinY(), getMinZ()),
                new Vector(getMaxX(), getMinY(), getMinZ()),
                new Vector(getMaxX(), getMaxY(), getMinZ()),
                new Vector(getMinX(), getMaxY(), getMinZ()),
        };
    }

    /**
     * Gets an array of {@link Vector} representing the points at the back of the {@link BoundingBox}.
     *
     * @return the points at the back of the {@link BoundingBox}
     */
    @NotNull
    public Vector[] getBackFace() {
        return new Vector[]{
                new Vector(getMinX(), getMinY(), getMaxZ()),
                new Vector(getMaxX(), getMinY(), getMaxZ()),
                new Vector(getMaxX(), getMaxY(), getMaxZ()),
                new Vector(getMinX(), getMaxY(), getMaxZ()),
        };
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
}
