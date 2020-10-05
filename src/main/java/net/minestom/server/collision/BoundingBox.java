package net.minestom.server.collision;

import net.minestom.server.entity.Entity;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.Vector;

/**
 * See https://wiki.vg/Entity_metadata#Mobs_2
 */
public class BoundingBox {

    private final Entity entity;
    private float x, y, z;

    /**
     * Create a {@link BoundingBox} linked to an {@link Entity} and with a specific size
     *
     * @param entity the linked entity
     * @param x      the width size
     * @param y      the height size
     * @param z      the depth size
     */
    public BoundingBox(Entity entity, float x, float y, float z) {
        this.entity = entity;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Used to know if two {@link BoundingBox} intersect with each other
     *
     * @param boundingBox the {@link BoundingBox} to check
     * @return true if the two {@link BoundingBox} intersect with each other, false otherwise
     */
    public boolean intersect(BoundingBox boundingBox) {
        return (getMinX() <= boundingBox.getMaxX() && getMaxX() >= boundingBox.getMinX()) &&
                (getMinY() <= boundingBox.getMaxY() && getMaxY() >= boundingBox.getMinY()) &&
                (getMinZ() <= boundingBox.getMaxZ() && getMaxZ() >= boundingBox.getMinZ());
    }

    /**
     * Used to know if this {@link BoundingBox} intersects with the bounding box of an entity
     *
     * @param entity the entity to check the bounding box
     * @return true if this bounding box intersects with the entity, false otherwise
     */
    public boolean intersect(Entity entity) {
        return intersect(entity.getBoundingBox());
    }

    /**
     * Used to know if the bounding box intersects at a {@link BlockPosition}
     *
     * @param blockPosition the position to check
     * @return true if the bounding box intersects with the position, false otherwise
     */
    public boolean intersect(BlockPosition blockPosition) {

        final float offsetX = 1;
        final float x = blockPosition.getX();
        final float minX = x;
        final float maxX = x + offsetX;

        final boolean checkX = getMinX() < maxX && getMaxX() > minX;
        if (!checkX)
            return false;

        final float y = blockPosition.getY();
        final float minY = y;
        final float maxY = y + 0.99999f;

        final boolean checkY = getMinY() < maxY && getMaxY() > minY;
        if (!checkY)
            return false;

        final float offsetZ = 1;
        final float z = blockPosition.getZ();
        final float minZ = z;
        final float maxZ = z + offsetZ;

        final boolean checkZ = getMinZ() < maxZ && getMaxZ() > minZ;
        return checkZ;
    }

    public boolean intersect(float x, float y, float z) {
        return (x >= getMinX() && x <= getMaxX()) &&
                (y >= getMinY() && y <= getMaxY()) &&
                (z >= getMinZ() && z <= getMaxZ());
    }

    public boolean intersect(Position position) {
        return intersect(position.getX(), position.getY(), position.getZ());
    }

    /**
     * Create a new {@link BoundingBox} linked to the same {@link Entity} with expanded size
     *
     * @param x the X offset
     * @param y the Y offset
     * @param z the Z offset
     * @return a new {@link BoundingBox} expanded
     */
    public BoundingBox expand(float x, float y, float z) {
        return new BoundingBox(entity, this.x + x, this.y + y, this.z + z);
    }

    /**
     * Create a new {@link BoundingBox} linked to the same {@link Entity} with contracted size
     *
     * @param x the X offset
     * @param y the Y offset
     * @param z the Z offset
     * @return a new bounding box contracted
     */
    public BoundingBox contract(float x, float y, float z) {
        return new BoundingBox(entity, this.x - x, this.y - y, this.z - z);
    }

    /**
     * Get the width of the {@link BoundingBox}
     *
     * @return the width
     */
    public float getWidth() {
        return x;
    }

    /**
     * Get the height of the {@link BoundingBox}
     *
     * @return the height
     */
    public float getHeight() {
        return y;
    }

    /**
     * Get the depth of the {@link BoundingBox}
     *
     * @return the depth
     */
    public float getDepth() {
        return z;
    }

    /**
     * Get the min X based on {@link #getWidth()} and the {@link Entity} position
     *
     * @return the min X
     */
    public float getMinX() {
        return entity.getPosition().getX() - (x / 2);
    }

    /**
     * Get the max X based on {@link #getWidth()} and the {@link Entity} position
     *
     * @return the max X
     */
    public float getMaxX() {
        return entity.getPosition().getX() + (x / 2);
    }

    /**
     * Get the min Y based on the {@link Entity} position
     *
     * @return the min Y
     */
    public float getMinY() {
        return entity.getPosition().getY();
    }

    /**
     * Get the max Y based on {@link #getHeight()} and the {@link Entity} position
     *
     * @return the max Y
     */
    public float getMaxY() {
        return entity.getPosition().getY() + y;
    }

    /**
     * Get the min Z based on {@link #getDepth()} and the {@link Entity} position
     *
     * @return the min Z
     */
    public float getMinZ() {
        return entity.getPosition().getZ() - (z / 2);
    }

    /**
     * Get the max Z based on {@link #getDepth()} and the {@link Entity} position
     *
     * @return the max Z
     */
    public float getMaxZ() {
        return entity.getPosition().getZ() + (z / 2);
    }

    /**
     * Get an array of {@link Vector} representing the points at the bottom of the {@link BoundingBox}
     *
     * @return the points at the bottom of the {@link BoundingBox}
     */
    public Vector[] getBottomFace() {
        return new Vector[]{
                new Vector(getMinX(), getMinY(), getMinZ()),
                new Vector(getMaxX(), getMinY(), getMinZ()),
                new Vector(getMaxX(), getMinY(), getMaxZ()),
                new Vector(getMinX(), getMinY(), getMaxZ()),
        };
    }

    /**
     * Get an array of {@link Vector} representing the points at the top of the {@link BoundingBox}
     *
     * @return the points at the top of the {@link BoundingBox}
     */
    public Vector[] getTopFace() {
        return new Vector[]{
                new Vector(getMinX(), getMaxY(), getMinZ()),
                new Vector(getMaxX(), getMaxY(), getMinZ()),
                new Vector(getMaxX(), getMaxY(), getMaxZ()),
                new Vector(getMinX(), getMaxY(), getMaxZ()),
        };
    }

    /**
     * Get an array of {@link Vector} representing the points on the left face of the {@link BoundingBox}
     *
     * @return the points on the left face of the {@link BoundingBox}
     */
    public Vector[] getLeftFace() {
        return new Vector[]{
                new Vector(getMinX(), getMinY(), getMinZ()),
                new Vector(getMinX(), getMaxY(), getMinZ()),
                new Vector(getMinX(), getMaxY(), getMaxZ()),
                new Vector(getMinX(), getMinY(), getMaxZ()),
        };
    }

    /**
     * Get an array of {@link Vector} representing the points on the right face of the {@link BoundingBox}
     *
     * @return the points on the right face of the {@link BoundingBox}
     */
    public Vector[] getRightFace() {
        return new Vector[]{
                new Vector(getMaxX(), getMinY(), getMinZ()),
                new Vector(getMaxX(), getMaxY(), getMinZ()),
                new Vector(getMaxX(), getMaxY(), getMaxZ()),
                new Vector(getMaxX(), getMinY(), getMaxZ()),
        };
    }

    /**
     * Get an array of {@link Vector} representing the points at the front of the {@link BoundingBox}
     *
     * @return the points at the front of the {@link BoundingBox}
     */
    public Vector[] getFrontFace() {
        return new Vector[]{
                new Vector(getMinX(), getMinY(), getMinZ()),
                new Vector(getMaxX(), getMinY(), getMinZ()),
                new Vector(getMaxX(), getMaxY(), getMinZ()),
                new Vector(getMinX(), getMaxY(), getMinZ()),
        };
    }

    /**
     * Get an array of {@link Vector} representing the points at the back of the {@link BoundingBox}
     *
     * @return the points at the back of the {@link BoundingBox}
     */
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
