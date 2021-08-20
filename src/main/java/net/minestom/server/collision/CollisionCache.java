package net.minestom.server.collision;

import kotlin.Pair;
import kotlin.Triple;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Chunk;

import java.util.Objects;

public class CollisionCache {
    final long cachedAt;
    final boolean wasOnGround;
    final int cornerHash;
    final Entity entity;

    public CollisionCache(long cachedAt, boolean wasOnGround, Entity entity) {
        this.cachedAt = cachedAt;
        final Pos position = entity.getPosition();
        this.cornerHash = hashCorners(position.x(), position.y(), position.z(), entity);
        this.wasOnGround = wasOnGround;
        this.entity = entity;
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
        final int cornerHash = hashCorners(position.x(), position.y(), position.z(), entity);
        return new Pair<>(this.cornerHash == cornerHash, velocity);
    }

    public static int hashCorners(double x, double y, double z, Entity entity) {
        final double sizeX = entity.getBoundingBox().getWidth() / 2, sizeY = entity.getBoundingBox().getHeight() / 2, sizeZ = entity.getBoundingBox().getDepth() / 2;
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
