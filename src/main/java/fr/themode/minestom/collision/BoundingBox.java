package fr.themode.minestom.collision;

import fr.themode.minestom.entity.Entity;

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

}
