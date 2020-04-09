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
        final float x = 1.6f;
        final float y = 1;
        final float z = 1.6f;
        float minX = blockPosition.getX() - (x / 2) + 0.5f;
        float maxX = blockPosition.getX() + (x / 2) + 0.5f;

        float minY = blockPosition.getY();
        float maxY = blockPosition.getY() + y;

        float minZ = blockPosition.getZ() - (z / 2) + 0.5f;
        float maxZ = blockPosition.getZ() + (z / 2) + 0.5f;

        boolean checkX = getMinX() + x / 2 < maxX && getMaxX() - x / 2 > minX;
        boolean checkY = getMinY() + y < maxY && getMaxY() + y > minY;
        boolean checkZ = getMinZ() + z / 2 < maxZ && getMaxZ() - z / 2 > minZ;
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
        return entity.getPosition().getY() - (y / 2);
    }

    public float getMaxY() {
        return entity.getPosition().getY() + (y / 2);
    }

    public float getMinZ() {
        return entity.getPosition().getZ() - (z / 2);
    }

    public float getMaxZ() {
        return entity.getPosition().getZ() + (z / 2);
    }

    @Override
    public String toString() {
        return "BoundingBox[" + x + ":" + y + ":" + z + "]";
    }
}
