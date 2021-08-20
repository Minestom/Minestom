package net.minestom.server.collision;

import kotlin.Pair;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;

import java.util.Objects;

public class CollisionCache {
    long cachedAt;
    boolean wasOnGround;
    int cornerHash;
    final Entity entity;

    public CollisionCache(Entity entity) {
        this.entity = entity;
        update(entity.getPosition());
    }
    
    public void update(Point position) {
        this.cachedAt = System.currentTimeMillis();
        this.cornerHash = hashCorners(position.x(), position.y(), position.z(), entity.getBoundingBox());
        this.wasOnGround = entity.isOnGround();
    }

    /**
     * @return first - is cache valid; second - velocity (redundant if first is false)
     */
    public Pair<Boolean, Vec> isValid(Vec velocity) {
        if (entity.getChunk().getLastChangeTime() > cachedAt) {
            // Chunk changed
            return new Pair<>(false, null);
        }
        if (wasOnGround && velocity.y() <= 0) {
            // Ignore negative/zero y
            velocity = velocity.withY(0);
        }

        final Pos position = entity.getPosition();
        final int cornerHash = hashCorners(position.x(), position.y(), position.z(), entity.getBoundingBox());
        return new Pair<>(this.cornerHash == cornerHash, velocity);
    }

    public static int hashCorners(double x, double y, double z, BoundingBox box) {
        final double sizeX = box.getWidth() / 2, sizeY = box.getHeight() / 2, sizeZ = box.getDepth() / 2;
        return Objects.hash(
                Math.floor(x - sizeX),
                Math.floor(y - sizeY),
                Math.floor(z - sizeZ),
                Math.floor(x + sizeX),
                Math.floor(y + sizeY),
                Math.floor(z + sizeZ)
        );
    }
}
