package fr.themode.minestom.collision;

import fr.themode.minestom.entity.Entity;
import fr.themode.minestom.utils.BlockPosition;
import fr.themode.minestom.utils.Position;

/**
 * See https://wiki.vg/Entity_metadata#Mobs_2
 */
public class BoundingBox {

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

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
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
