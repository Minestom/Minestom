package net.minestom.server.collision;

import net.minestom.server.entity.Entity;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.Vector;

/**
 * See https://wiki.vg/Entity_metadata#Mobs_2
 */
public class BoundingBox {
// TODO:

    private Entity entity;
    private float x, y, z;

    public BoundingBox(Entity entity, float x, float y, float z) {
        this.entity = entity;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public boolean intersect(BoundingBox boundingBox) {
        return (getMinX() <= boundingBox.getMaxX() && getMaxX() >= boundingBox.getMinX()) &&
                (getMinY() <= boundingBox.getMaxY() && getMaxY() >= boundingBox.getMinY()) &&
                (getMinZ() <= boundingBox.getMaxZ() && getMaxZ() >= boundingBox.getMinZ());
    }

    public boolean intersect(BlockPosition blockPosition) {

        final float x = blockPosition.getX();
        final float y = blockPosition.getY();
        final float z = blockPosition.getZ();

        final float offsetX = 1;
        final float offsetZ = 1;

        float minX = x;
        float maxX = x + offsetX;

        float minY = y;
        float maxY = y + 0.99999f;

        float minZ = z;
        float maxZ = z + offsetZ;

        boolean checkX = getMinX() < maxX && getMaxX() > minX;
        boolean checkY = getMinY() < maxY && getMaxY() > minY;
        boolean checkZ = getMinZ() < maxZ && getMaxZ() > minZ;

        return checkX && checkY && checkZ;
    }

    public boolean intersect(float x, float y, float z) {
        return (x >= getMinX() && x <= getMaxX()) &&
                (y >= getMinY() && y <= getMaxY()) &&
                (z >= getMinZ() && z <= getMaxZ());
    }

    public boolean intersect(Position position) {
        return intersect(position.getX(), position.getY(), position.getZ());
    }

    public BoundingBox expand(float x, float y, float z) {
        return new BoundingBox(entity, this.x + x, this.y + y, this.z + z);
    }

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
        return new Vector[] {
                new Vector(getMinX(), getMinY(), getMinZ()),
                new Vector(getMaxX(), getMinY(), getMinZ()),
                new Vector(getMaxX(), getMinY(), getMaxZ()),
                new Vector(getMinX(), getMinY(), getMaxZ()),
        };
    }

    public Vector[] getTopFace() {
        return new Vector[] {
                new Vector(getMinX(), getMaxY(), getMinZ()),
                new Vector(getMaxX(), getMaxY(), getMinZ()),
                new Vector(getMaxX(), getMaxY(), getMaxZ()),
                new Vector(getMinX(), getMaxY(), getMaxZ()),
        };
    }

    public Vector[] getLeftFace() {
        return new Vector[] {
                new Vector(getMinX(), getMinY(), getMinZ()),
                new Vector(getMinX(), getMaxY(), getMinZ()),
                new Vector(getMinX(), getMaxY(), getMaxZ()),
                new Vector(getMinX(), getMinY(), getMaxZ()),
        };
    }

    public Vector[] getRightFace() {
        return new Vector[] {
                new Vector(getMaxX(), getMinY(), getMinZ()),
                new Vector(getMaxX(), getMaxY(), getMinZ()),
                new Vector(getMaxX(), getMaxY(), getMaxZ()),
                new Vector(getMaxX(), getMinY(), getMaxZ()),
        };
    }

    public Vector[] getFrontFace() {
        return new Vector[] {
                new Vector(getMinX(), getMinY(), getMinZ()),
                new Vector(getMaxX(), getMinY(), getMinZ()),
                new Vector(getMaxX(), getMaxY(), getMinZ()),
                new Vector(getMinX(), getMaxY(), getMinZ()),
        };
    }

    public Vector[] getBackFace() {
        return new Vector[] {
                new Vector(getMinX(), getMinY(), getMaxZ()),
                new Vector(getMaxX(), getMinY(), getMaxZ()),
                new Vector(getMaxX(), getMaxY(), getMaxZ()),
                new Vector(getMinX(), getMaxY(), getMaxZ()),
        };
    }

    @Override
    public String toString() {
        String result = "BoudingBox";
        result += "\n";
        result += "[" + getMinX() + " : " + getMaxX() + "]";
        result += "\n";
        result += "[" + getMinY() + " : " + getMaxY() + "]";
        result += "\n";
        result += "[" + getMinZ() + " : " + getMaxZ() + "]";
        return result;
    }
}
