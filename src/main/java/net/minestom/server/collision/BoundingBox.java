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

    public BoundingBox(Entity entity, float x, float y, float z) {
        this.entity = entity;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Used to know if two BoundingBox intersect with each other
     *
     * @param boundingBox the bounding box to check
     * @return true if the two BoundingBox intersect with each other, false otherwise
     */
    public boolean intersect(BoundingBox boundingBox) {
        return (getMinX() <= boundingBox.getMaxX() && getMaxX() >= boundingBox.getMinX()) &&
                (getMinY() <= boundingBox.getMaxY() && getMaxY() >= boundingBox.getMinY()) &&
                (getMinZ() <= boundingBox.getMaxZ() && getMaxZ() >= boundingBox.getMinZ());
    }

    /**
     * Used to know if this bounding box intersects with the bounding box of an entity
     *
     * @param entity the entity to check the bounding box
     * @return true if this bounding box intersects with the entity, false otherwise
     */
    public boolean intersect(Entity entity) {
        return intersect(entity.getBoundingBox());
    }

    /**
     * Used to know if the bounding box intersects with a block (can be air)
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
     * Create a new bounding box linked to the same entity with expanded size
     *
     * @param x the X offset
     * @param y the Y offset
     * @param z the Z offset
     * @return a new bounding box expanded
     */
    public BoundingBox expand(float x, float y, float z) {
        return new BoundingBox(entity, this.x + x, this.y + y, this.z + z);
    }

    /**
     * Create a new bounding box linked to the same entity with contracted size
     *
     * @param x the X offset
     * @param y the Y offset
     * @param z the Z offset
     * @return a new bounding box contracted
     */
    public BoundingBox contract(float x, float y, float z) {
        return new BoundingBox(entity, this.x - x, this.y - y, this.z - z);
    }

    public float getWidth() {
        return x;
    }

    public float getHeight() {
        return y;
    }

    public float getDepth() {
        return z;
    }

    public float getMinX() {
        return entity.getPosition().getX() - (x / 2);
    }

    public float getMaxX() {
        return entity.getPosition().getX() + (x / 2);
    }

    public float getMinY() {
        return entity.getPosition().getY();
    }

    public float getMaxY() {
        return entity.getPosition().getY() + y;
    }

    public float getMinZ() {
        return entity.getPosition().getZ() - (z / 2);
    }

    public float getMaxZ() {
        return entity.getPosition().getZ() + (z / 2);
    }

    public Vector[] getBottomFace() {
        return new Vector[]{
                new Vector(getMinX(), getMinY(), getMinZ()),
                new Vector(getMaxX(), getMinY(), getMinZ()),
                new Vector(getMaxX(), getMinY(), getMaxZ()),
                new Vector(getMinX(), getMinY(), getMaxZ()),
        };
    }

    public Vector[] getTopFace() {
        return new Vector[]{
                new Vector(getMinX(), getMaxY(), getMinZ()),
                new Vector(getMaxX(), getMaxY(), getMinZ()),
                new Vector(getMaxX(), getMaxY(), getMaxZ()),
                new Vector(getMinX(), getMaxY(), getMaxZ()),
        };
    }

    public Vector[] getLeftFace() {
        return new Vector[]{
                new Vector(getMinX(), getMinY(), getMinZ()),
                new Vector(getMinX(), getMaxY(), getMinZ()),
                new Vector(getMinX(), getMaxY(), getMaxZ()),
                new Vector(getMinX(), getMinY(), getMaxZ()),
        };
    }

    public Vector[] getRightFace() {
        return new Vector[]{
                new Vector(getMaxX(), getMinY(), getMinZ()),
                new Vector(getMaxX(), getMaxY(), getMinZ()),
                new Vector(getMaxX(), getMaxY(), getMaxZ()),
                new Vector(getMaxX(), getMinY(), getMaxZ()),
        };
    }

    public Vector[] getFrontFace() {
        return new Vector[]{
                new Vector(getMinX(), getMinY(), getMinZ()),
                new Vector(getMaxX(), getMinY(), getMinZ()),
                new Vector(getMaxX(), getMaxY(), getMinZ()),
                new Vector(getMinX(), getMaxY(), getMinZ()),
        };
    }

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
