package net.minestom.server.collision;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Nullable;

public class CollisionCache {
    private final Entity entity;
    long cachedAt;
    boolean wasOnGround;
    int cornerHash;

    private Instance instance;
    private CollisionUtils.PhysicsResult physicsResult;

    public CollisionCache(Entity entity) {
        this.entity = entity;
    }

    public void update(CollisionUtils.PhysicsResult result) {
        final Pos position = result.newPosition();
        this.instance = entity.getInstance();
        this.physicsResult = result;
        this.cachedAt = System.currentTimeMillis();
        this.cornerHash = hashCorners(position.x(), position.y(), position.z(), entity.getBoundingBox());
        this.wasOnGround = entity.isOnGround();
    }

    /**
     * @return the new entity velocity, null if this should be fully computed
     */
    public @Nullable Vec getCachedVelocity(Vec velocity) {
        if (entity.getInstance() != instance) return null;
        final Chunk currentChunk = entity.getChunk();
        // Check chunk change
        if (currentChunk == null || currentChunk.getLastChangeTime() > cachedAt) return null;
        if (wasOnGround && velocity.y() <= 0) {
            // Ignore negative/zero y
            velocity = velocity.withY(0);
        }

        final Pos position = entity.getPosition();
        final int cornerHash = hashCorners(position.x(), position.y(), position.z(), entity.getBoundingBox());
        if (this.cornerHash != cornerHash) return null;
        return velocity;
    }

    public @Nullable CollisionUtils.PhysicsResult getLastPhysicsResult() {
        return physicsResult;
    }

    public static int hashCorners(double x, double y, double z, BoundingBox box) {
        final double sizeX = box.getWidth() / 2, sizeY = box.getHeight() / 2, sizeZ = box.getDepth() / 2;
        int result = 1;
        result = apply(result, Math.floor(x - sizeX));
        result = apply(result, Math.floor(y - sizeY));
        result = apply(result, Math.floor(z - sizeZ));

        result = apply(result, Math.floor(x + sizeX));
        result = apply(result, Math.floor(y + sizeY));
        result = apply(result, Math.floor(z + sizeZ));
        return result;
    }

    private static int apply(int result, double element) {
        final long bits = Double.doubleToLongBits(element);
        return 31 * result + (int) (bits ^ (bits >>> 32));
    }
}
