package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.WorldBorder;
import net.minestom.server.instance.block.Block;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.chunk.ChunkCache;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Function;

@ApiStatus.Internal
public final class CollisionUtils {

    /**
     * Moves an entity with physics applied (ie checking against blocks)
     * <p>
     * Works by getting all the full blocks that an entity could interact with.
     * All bounding boxes inside the full blocks are checked for collisions with the entity.
     *
     * @param entity            the entity to move
     * @param entityVelocity    the velocity of the entity
     * @param lastPhysicsResult the last physics result, can be null
     * @param singleCollision   if the entity should only collide with one block
     * @return the result of physics simulation
     */
    public static PhysicsResult handlePhysics(@NotNull Entity entity, @NotNull Vec entityVelocity,
                                              @Nullable PhysicsResult lastPhysicsResult, boolean singleCollision) {
        final Instance instance = entity.getInstance();
        assert instance != null;
        return handlePhysics(instance, entity.getChunk(),
                entity.getBoundingBox(),
                entity.getPosition(), entityVelocity,
                lastPhysicsResult, singleCollision);
    }

    /**
     * Checks for entity collisions
     *
     * @param velocity     the velocity of the entity
     * @param extendRadius the largest entity bounding box we can collide with
     *                     Measured from bottom center to top corner
     *                     This is used to extend the search radius for entities we collide with
     *                     For players this is (0.3^2 + 0.3^2 + 1.8^2) ^ (1/3) ~= 1.51
     */
    public static @NotNull Collection<EntityCollisionResult> checkEntityCollisions(@NotNull Instance instance, @NotNull BoundingBox boundingBox, @NotNull Point pos, @NotNull Vec velocity, double extendRadius, @NotNull Function<Entity, Boolean> entityFilter, @Nullable PhysicsResult physicsResult) {
        return EntityCollision.checkCollision(instance, boundingBox, pos, velocity, extendRadius, entityFilter, physicsResult);
    }

    /**
     * Checks for entity collisions
     *
     * @param entity        the entity to check collisions for
     * @param velocity      the velocity of the entity
     * @param extendRadius  the largest entity bounding box we can collide with
     * @param entityFilter  the entity filter
     * @param physicsResult optional physics result
     * @return the entity collision results
     */
    public static @NotNull Collection<EntityCollisionResult> checkEntityCollisions(@NotNull Entity entity, @NotNull Vec velocity, double extendRadius, @NotNull Function<Entity, Boolean> entityFilter, @Nullable PhysicsResult physicsResult) {
        return EntityCollision.checkCollision(entity.getInstance(), entity.getBoundingBox(), entity.getPosition(), velocity, extendRadius, entityFilter, physicsResult);
    }

    /**
     * Moves an entity with physics applied (ie checking against blocks)
     * <p>
     * Works by getting all the full blocks that an entity could interact with.
     * All bounding boxes inside the full blocks are checked for collisions with the entity.
     *
     * @param entity            the entity to move
     * @param entityVelocity    the velocity of the entity
     * @param lastPhysicsResult the last physics result, can be null
     * @return the result of physics simulation
     */
    public static PhysicsResult handlePhysics(@NotNull Entity entity, @NotNull Vec entityVelocity,
                                              @Nullable PhysicsResult lastPhysicsResult) {
        final Instance instance = entity.getInstance();
        assert instance != null;
        return handlePhysics(instance, entity.getChunk(),
                entity.getBoundingBox(),
                entity.getPosition(), entityVelocity,
                lastPhysicsResult, false);
    }

    /**
     * Moves bounding box with physics applied (ie checking against blocks)
     * <p>
     * Works by getting all the full blocks that a bounding box could interact with.
     * All bounding boxes inside the full blocks are checked for collisions with the given bounding box.
     *
     * @param boundingBox the bounding box to move
     * @return the result of physics simulation
     */
    public static PhysicsResult handlePhysics(@NotNull Instance instance, @Nullable Chunk chunk,
                                              @NotNull BoundingBox boundingBox,
                                              @NotNull Pos position, @NotNull Vec velocity,
                                              @Nullable PhysicsResult lastPhysicsResult, boolean singleCollision) {
        final Block.Getter getter = new ChunkCache(instance, chunk != null ? chunk : instance.getChunkAt(position), Block.STONE);
        return handlePhysics(getter, boundingBox, position, velocity, lastPhysicsResult, singleCollision);
    }

    /**
     * Moves bounding box with physics applied (ie checking against blocks)
     * <p>
     * Works by getting all the full blocks that a bounding box could interact with.
     * All bounding boxes inside the full blocks are checked for collisions with the given bounding box.
     *
     * @param blockGetter the block getter to check collisions against, ensure block access is synchronized
     * @return the result of physics simulation
     */
    @ApiStatus.Internal
    public static PhysicsResult handlePhysics(@NotNull Block.Getter blockGetter,
                                              @NotNull BoundingBox boundingBox,
                                              @NotNull Pos position, @NotNull Vec velocity,
                                              @Nullable PhysicsResult lastPhysicsResult, boolean singleCollision) {
        return BlockCollision.handlePhysics(boundingBox,
                velocity, position,
                blockGetter, lastPhysicsResult, singleCollision);
    }

    /**
     * Checks whether shape is reachable by the given line of sight
     * (ie there are no blocks colliding with it).
     *
     * @param instance the instance.
     * @param chunk    optional chunk reference for speedup purposes.
     * @param start    start of the line of sight.
     * @param end      end of the line of sight.
     * @param shape    shape to check.
     * @return true is shape is reachable by the given line of sight; false otherwise.
     */
    public static boolean isLineOfSightReachingShape(@NotNull Instance instance, @Nullable Chunk chunk,
                                                     @NotNull Point start, @NotNull Point end,
                                                     @NotNull Shape shape) {
        final PhysicsResult result = handlePhysics(instance, chunk,
                BoundingBox.ZERO,
                Pos.fromPoint(start), Vec.fromPoint(end.sub(start)),
                null, false);

        return shape.intersectBox(end.sub(result.newPosition()).sub(Vec.EPSILON), BoundingBox.ZERO);
    }

    public static PhysicsResult handlePhysics(@NotNull Entity entity, @NotNull Vec entityVelocity) {
        return handlePhysics(entity, entityVelocity, null);
    }

    public static Entity canPlaceBlockAt(Instance instance, Point blockPos, Block b) {
        return BlockCollision.canPlaceBlockAt(instance, blockPos, b);
    }

    /**
     * Applies world border collision.
     *
     * @param worldBorder     the world border
     * @param currentPosition the current position
     * @param newPosition     the future target position
     * @return the position with the world border collision applied (can be {@code newPosition} if not changed)
     */
    public static @NotNull Pos applyWorldBorder(@NotNull WorldBorder worldBorder, @NotNull Pos currentPosition, @NotNull Pos newPosition) {
        double radius = worldBorder.diameter() / 2;
        // If there is a collision on a given axis prevent the entity
        // from moving forward by supplying their previous position's value
        boolean xCollision = newPosition.x() > worldBorder.centerX() + radius || newPosition.x() < worldBorder.centerX() - radius;
        boolean zCollision = newPosition.z() > worldBorder.centerZ() + radius || newPosition.z() < worldBorder.centerZ() - radius;
        if (xCollision || zCollision) {
            return newPosition.withCoord(xCollision ? currentPosition.x() : newPosition.x(), newPosition.y(),
                    zCollision ? currentPosition.z() : newPosition.z());
        }
        return newPosition;
    }

    public static Shape parseBlockShape(String collision, String occlusion, Registry.BlockEntry blockEntry) {
        return ShapeImpl.parseBlockFromRegistry(collision, occlusion, blockEntry.occludes(), blockEntry.lightEmission());
    }

    /**
     * Simulate the entity's collision physics as if the world had no blocks
     *
     * @param entityPosition the position of the entity
     * @param entityVelocity the velocity of the entity
     * @return the result of physics simulation
     */
    public static PhysicsResult blocklessCollision(@NotNull Pos entityPosition, @NotNull Vec entityVelocity) {
        return new PhysicsResult(entityPosition.add(entityVelocity), entityVelocity, false,
                false, false, false, entityVelocity, new Point[3],
                new Shape[3], new Point[3], false, SweepResult.NO_COLLISION);
    }
}
