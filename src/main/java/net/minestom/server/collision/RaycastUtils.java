package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Utils class for {@link Ray}.
 */
class RaycastUtils
{
    /**
     * Raycasting section
     */
    private static final double NORMAL_EPSILON = 0.000001;
    private static final Vec MAX_X_NORMAL = new Vec(1, 0, 0);
    private static final Vec MIN_X_NORMAL = new Vec(-1, 0, 0);
    private static final Vec MAX_Y_NORMAL = new Vec(0, 1, 0);
    private static final Vec MIN_Y_NORMAL = new Vec(0, -1, 0);
    private static final Vec MAX_Z_NORMAL = new Vec(0, 0, 1);
    private static final Vec MIN_Z_NORMAL = new Vec(0, 0, -1);

    static @NotNull EntityBlockCastResult performEntityBlockCast(@NotNull Ray ray, @NotNull Block.Getter blockGetter, @NotNull Collection<Entity> entities) {
        List<BlockCastResult.BlockRayCollision> blockCollisions = performBlockCast(ray, blockGetter).blockCollisions();
        List<EntityCastResult.EntityRayCollision> entityCollisions = performEntityCast(ray, entities).entityCollisions();
        // Create an ordered list with all results
        ArrayList<CastResult.RayCollision> allRayCollisions = new ArrayList<>(blockCollisions.size() + entityCollisions.size());
        allRayCollisions.addAll(blockCollisions);
        allRayCollisions.addAll(entityCollisions);
        allRayCollisions.sort(Comparator.comparingDouble(a -> a.entry().distanceSquared(ray.origin())));
        return new EntityBlockRaycastResultImpl(entityCollisions, blockCollisions, allRayCollisions);
    }

    static @NotNull BlockCastResult performBlockCast(@NotNull Ray ray, @NotNull Block.Getter blockGetter) {
        Vec reciprocal = new Vec(1 / ray.direction().x(), 1 / ray.direction().y(), 1 / ray.direction().z());
        return new BlockCastResultImpl(findIntersectingBlocks(ray, blockGetter, reciprocal));
    }

    static @NotNull EntityCastResult performEntityCast(@NotNull Ray ray, @NotNull Collection<Entity> entities) {
        Vec reciprocal = new Vec(1 / ray.direction().x(), 1 / ray.direction().y(), 1 / ray.direction().z());
        return new EntityCastResultImpl(findIntersectingEntities(ray, entities, reciprocal));
    }

    /**
     * @param ray            the casting ray
     * @param blockGetter    the getter containing the target blocks
     * @param reciprocal     the pre-computed ray direction reciprocal (1/vec)
     * @return               the intersecting block ray collision list
     */
    private static @NotNull List<BlockCastResult.BlockRayCollision> findIntersectingBlocks(@NotNull Ray ray, @NotNull Block.Getter blockGetter,
                                                                                           @NotNull Vec reciprocal) {
        // Grab every block coordinate that the ray passes through using
        // a 3d digital differential analyzer (line drawing) algorithm
        boolean infiniteX = Double.isInfinite(reciprocal.x()), infiniteY = Double.isInfinite(reciprocal.y()), infiniteZ = Double.isInfinite(reciprocal.z());

        // Initialization phase
        Point origin = ray.origin();
        Vec direction = ray.direction();
        int blockX = origin.blockX();
        int blockY = origin.blockY();
        int blockZ = origin.blockZ();
        final int stepX = direction.x() > 0 ? 1 : -1;
        final int stepY = direction.y() > 0 ? 1 : -1;
        final int stepZ = direction.z() > 0 ? 1 : -1;
        final double deltaX = Math.abs(1 / direction.x());
        final double deltaY = Math.abs(1 / direction.y());
        final double deltaZ = Math.abs(1 / direction.z());
        double currentX = (direction.x() > 0 ? blockX + 1 - origin.x() : origin.x() - blockX) * deltaX;
        double currentY = (direction.y() > 0 ? blockY + 1 - origin.y() : origin.y() - blockY) * deltaY;
        double currentZ = (direction.z() > 0 ? blockZ + 1 - origin.z() : origin.z() - blockZ) * deltaZ;
        if (Double.isNaN(currentX)) currentX = Double.POSITIVE_INFINITY;
        if (Double.isNaN(currentY)) currentY = Double.POSITIVE_INFINITY;
        if (Double.isNaN(currentZ)) currentZ = Double.POSITIVE_INFINITY;

        int collisionLimit = ray.configuration().blockCollisionLimit();
        final List<BlockCastResult.BlockRayCollision> collisions = new ArrayList<>(Math.min(collisionLimit, (int) ray.distance() + 1));
        // Execution phase
        // Test for block at the beginning of the ray
        int collisionCount = appendSuccessfulBlockCollisions(ray, blockGetter, blockX, blockY, blockZ, collisions, 0,
                reciprocal, infiniteX, infiniteY, infiniteZ);

        while (collisionCount < collisionLimit && Math.min(currentX, Math.min(currentY, currentZ)) <= ray.distance()) {
            // Travel the minimum distance needed to progress to the next block coordinate
            if (currentX < currentZ && currentX < currentY) {
                currentX += deltaX;
                blockX += stepX;
            }
            else if (currentZ < currentY) {
                currentZ += deltaZ;
                blockZ += stepZ;
            }
            else {
                currentY += deltaY;
                blockY += stepY;
            }

            collisionCount += appendSuccessfulBlockCollisions(ray, blockGetter, blockX, blockY, blockZ, collisions, collisionCount,
                    reciprocal, infiniteX, infiniteY, infiniteZ);
        }

        return collisions;
    }

    /**
     * @param ray            the casting ray
     * @param blockGetter    the getter containing the target block
     * @param blockX         the x coordinate of the block to check against
     * @param blockY         the y coordinate of the block to check against
     * @param blockZ         the z coordinate of the block to check against
     * @param collisions     the array to append any successful block collisions to
     * @param collisionCount the current collision count; for exiting when a block has
     *                       multiple bounding boxes and exceeds the config limit
     * @param reciprocal     the pre-computed ray direction reciprocal (1/vec)
     * @param infiniteX      pass true if the reciprocal x component is infinity
     * @param infiniteY      pass true if the reciprocal y component is infinity
     * @param infiniteZ      pass true if the reciprocal z component is infinity
     * @return               the amount of blocks added to the collisions array
     */
    private static int appendSuccessfulBlockCollisions(@NotNull Ray ray, @NotNull Block.Getter blockGetter,
                                                       int blockX, int blockY, int blockZ,
                                                       @NotNull List<BlockCastResult.BlockRayCollision> collisions,
                                                       int collisionCount,
                                                       @NotNull Vec reciprocal,
                                                       boolean infiniteX, boolean infiniteY, boolean infiniteZ) {
        Block block = blockGetter.getBlock(blockX, blockY, blockZ);
        if (block.isAir() || !ray.configuration().blockFilter().test(block)) return 0;
        BoundingBox[] boxes = ((ShapeImpl) block.registry().collisionShape()).getCollisionBoundingBoxes();
        if (boxes.length == 0) return 0;

        final double distanceSquared = ray.distance() * ray.distance();
        if (boxes.length == 1) {
            final BoundingBox box = boxes[0];
            CollisionResult result = findBoundingBoxCollision(ray, box, blockX, blockY, blockZ, reciprocal, infiniteX, infiniteY, infiniteZ);
            if (result != null && result.entry().distanceSquared(ray.origin()) <= distanceSquared) {
                collisions.add(createBlockRayCollision(result, block, box, blockX, blockY, blockZ, ray.configuration()));
                return 1;
            }
            return 0;
        }

        // Exit early if the collision limit if the block has multiple bounding boxes
        int collisionLimit = ray.configuration().blockCollisionLimit();
        List<BlockCastResult.BlockRayCollision> orderedCollisions = new ArrayList<>(boxes.length);
        for (BoundingBox box : boxes) {
            CollisionResult result = findBoundingBoxCollision(ray, box, blockX, blockY, blockZ, reciprocal, false, false, false);
            if (result != null && collisionCount < collisionLimit && result.entry().distanceSquared(ray.origin()) <= distanceSquared) {
                orderedCollisions.add(createBlockRayCollision(result, block, box, blockX, blockY, blockZ, ray.configuration()));
                collisionCount++;
            }
        }

        // Order multiple collisions by their distance to the ray origin
        orderedCollisions.sort(Comparator.comparingDouble(a -> a.entry().distanceSquared(ray.origin())));
        collisions.addAll(orderedCollisions);
        return orderedCollisions.size();
    }

    private static @NotNull BlockCastResult.BlockRayCollision createBlockRayCollision(@NotNull CollisionResult collisionResult, @NotNull Block block,
                                                                               @NotNull BoundingBox box, double offsetX, double offsetY, double offsetZ,
                                                                               @NotNull Ray.Configuration configuration) {
        Vec entrySurfaceNormal = null;
        Vec exitSurfaceNormal = null;
        if (configuration.computeSurfaceNormals()) {
            entrySurfaceNormal = computeSurfaceNormal(collisionResult.entry(), box, offsetX, offsetY, offsetZ);
            exitSurfaceNormal = computeSurfaceNormal(collisionResult.exit(), box, offsetX, offsetY, offsetZ);
        }
        return new BlockCastResult.BlockRayCollision(collisionResult.entry(), collisionResult.exit(), entrySurfaceNormal, exitSurfaceNormal, block);
    }

    /**
     * @param ray            the casting ray
     * @param entities       the entities to test intersections again
     * @param reciprocal     the pre-computed ray direction reciprocal (1/vec)
     * @return               the intersecting block ray collision list
     */
    private static @NotNull List<EntityCastResult.EntityRayCollision> findIntersectingEntities(@NotNull Ray ray, @NotNull Collection<Entity> entities,
                                                                                               @NotNull Vec reciprocal) {
        // Make the assumption that a ray is unlikely to intersect > 1 entity at once
        final List<EntityCastResult.EntityRayCollision> collisions = new ArrayList<>(1);
        final double maxDistanceSquared = ray.distance() * ray.distance();
        Vec boxExpansion = ray.configuration().entityBoundingBoxExpansion();
        boolean expandBoundingBox = !boxExpansion.isZero();
        boolean infiniteX = Double.isInfinite(reciprocal.x()), infiniteY = Double.isInfinite(reciprocal.y()), infiniteZ = Double.isInfinite(reciprocal.z());
        // Check entities
        for (Entity entity : entities) {
            if (!ray.configuration().entityFilter().test(entity)) continue;
            BoundingBox box = expandBoundingBox ? entity.getBoundingBox().expand(boxExpansion.x(), boxExpansion.y(), boxExpansion.z()) : entity.getBoundingBox();
            CollisionResult result = findBoundingBoxCollision(ray, box, entity.getPosition(), reciprocal, infiniteX, infiniteY, infiniteZ);
            if (result != null && result.entry().distanceSquared(ray.origin()) < maxDistanceSquared) {
                collisions.add(createEntityRayCollision(result, entity, box, ray.configuration()));
            }
        }

        // Sort by intersection order
        if (collisions.size() > 1) {
            collisions.sort(Comparator.comparingDouble(a -> a.entry().distanceSquared(ray.origin())));
        }
        return collisions;
    }

    private static @NotNull EntityCastResult.EntityRayCollision createEntityRayCollision(@NotNull CollisionResult collisionResult,
                                                                                         @NotNull Entity entity, @NotNull BoundingBox box,
                                                                                         @NotNull Ray.Configuration configuration) {
        Point offset = entity.getPosition();
        Vec entrySurfaceNormal = null;
        Vec exitSurfaceNormal = null;
        if (configuration.computeSurfaceNormals()) {
            entrySurfaceNormal = computeSurfaceNormal(collisionResult.entry(), box, offset.x(), offset.y(), offset.z());
            exitSurfaceNormal = computeSurfaceNormal(collisionResult.exit(), box, offset.x(), offset.y(), offset.z());
        }
        return new EntityCastResult.EntityRayCollision(collisionResult.entry(), collisionResult.exit(), entrySurfaceNormal, exitSurfaceNormal, entity);
    }

    /**
     * Get the entry/exit points of a ray on any given aabb bounding box.
     * Pre-calculating the reciprocal and infinities saves significant time.
     *
     * @param ray            the casting ray
     * @param box            the target bounding box
     * @param offsetX        the x offset of the box to in global space
     * @param offsetY        the y offset of the box to in global space
     * @param offsetZ        the z offset of the box to in global space
     * @param reciprocal     the pre-computed ray direction reciprocal (1/vec)
     * @param infiniteX      pass true if the reciprocal x component is infinity
     * @param infiniteY      pass true if the reciprocal y component is infinity
     * @param infiniteZ      pass true if the reciprocal z component is infinity
     * @return               a collision result if an intersection occurred, otherwise null
     */
    private static @Nullable RaycastUtils.CollisionResult findBoundingBoxCollision(@NotNull Ray ray, @NotNull BoundingBox box,
                                                                                   double offsetX, double offsetY, double offsetZ,
                                                                                   @NotNull Vec reciprocal,
                                                                                   boolean infiniteX, boolean infiniteY, boolean infiniteZ) {
        Point origin = ray.origin();
        Vec direction = ray.direction();
        // Determine the AABB ray intersections fast with the slab method
        // t[min/max] represents t in the parametric ray equation
        // ray.origin + ray.direction * t

        final double tx1, tx2;
        if (infiniteX) {
            // Explicitly set these to infinity to avoid an edge case
            // where 0 is multiplied by infinity
            tx1 = Double.NEGATIVE_INFINITY;
            tx2 = Double.POSITIVE_INFINITY;
        }
        else {
            tx1 = (box.minX() + offsetX - origin.x()) * reciprocal.x();
            tx2 = (box.minX() + box.width() + offsetX - origin.x()) * reciprocal.x();
        }

        final double ty1, ty2;
        if (infiniteY) {
            ty1 = Double.NEGATIVE_INFINITY;
            ty2 = Double.POSITIVE_INFINITY;
        }
        else {
            ty1 = (box.minY() + offsetY - origin.y()) * reciprocal.y();
            ty2 = (box.minY() + box.height() + offsetY - origin.y()) * reciprocal.y();
        }

        final double tz1, tz2;
        if (infiniteZ) {
            tz1 = Double.NEGATIVE_INFINITY;
            tz2 = Double.POSITIVE_INFINITY;
        }
        else {
            tz1 = (box.minZ() + offsetZ - origin.z()) * reciprocal.z();
            tz2 = (box.minZ() + box.depth() + offsetZ - origin.z()) * reciprocal.z();
        }

        double tEntry = Math.min(tx1, tx2);
        double tExit = Math.max(tx1, tx2);

        tEntry = Math.max(tEntry, Math.min(ty1, ty2));
        tExit = Math.min(tExit, Math.max(ty1, ty2));

        tEntry = Math.max(tEntry, Math.min(tz1, tz2));
        tExit = Math.min(tExit, Math.max(tz1, tz2));

        if (tEntry < 0 || tExit < tEntry) return null;
        return new CollisionResult(origin.add(direction.x() * tEntry, direction.y() * tEntry, direction.z() * tEntry),
                origin.add(direction.x() * tExit, direction.y() * tExit, direction.z() * tExit));
    }

    private static @Nullable CollisionResult findBoundingBoxCollision(@NotNull Ray ray, @NotNull BoundingBox box,
                                                                      @NotNull Point offset,
                                                                      @NotNull Vec reciprocal,
                                                                      boolean infiniteX, boolean infiniteY, boolean infiniteZ) {
        return findBoundingBoxCollision(ray, box, offset.x(), offset.y(), offset.z(), reciprocal, infiniteX, infiniteY, infiniteZ);
    }

    /**
     * Find the surface normal on a given collision.
     *
     * @return the normalized surface normal vec
     */
    private static @NotNull Vec computeSurfaceNormal(@NotNull Point intersection, @NotNull BoundingBox box,
                                                     double offsetX, double offsetY, double offsetZ) {
        if (Math.abs(intersection.x() - (box.maxX() + offsetX)) < NORMAL_EPSILON) return MAX_X_NORMAL;
        if (Math.abs(intersection.x() - (box.minX() + offsetX)) < NORMAL_EPSILON) return MIN_X_NORMAL;
        if (Math.abs(intersection.y() - (box.maxY() + offsetY)) < NORMAL_EPSILON) return MAX_Y_NORMAL;
        if (Math.abs(intersection.y() - (box.minY() + offsetY)) < NORMAL_EPSILON) return MIN_Y_NORMAL;
        if (Math.abs(intersection.z() - (box.maxZ() + offsetZ)) < NORMAL_EPSILON) return MAX_Z_NORMAL;
        else return MIN_Z_NORMAL;
    }

    /**
     * For returning collision data from {@link RaycastUtils#findBoundingBoxCollision}
     * to construct {@link CastResult} objects.
     */
    private record CollisionResult(@NotNull Point entry, @NotNull Point exit) {}


    /**
     * Ray helpers section
     */

    static @NotNull List<EntityCastResult.EntityRayCollision> findEntitiesBeforeEntityCollision(@NotNull List<CastResult.RayCollision> collisions, int collisionThreshold) {
        ArrayList<EntityCastResult.EntityRayCollision> foundCollisions = new ArrayList<>();
        int blockCollisionCount = 0;
        for (CastResult.RayCollision collision : collisions) {
            if (collision instanceof EntityCastResult.EntityRayCollision entityCollision) {
                foundCollisions.add(entityCollision);
                continue;
            }

            // Must be a block collision
            if (++blockCollisionCount == collisionThreshold) {
                break;
            }
        }

        return foundCollisions;
    }

    static @NotNull List<BlockCastResult.BlockRayCollision> findBlocksBeforeEntityCollision(@NotNull List<CastResult.RayCollision> collisions, int collisionThreshold) {
        ArrayList<BlockCastResult.BlockRayCollision> foundCollisions = new ArrayList<>();
        int blockCollisionCount = 0;
        for (CastResult.RayCollision collision : collisions) {
            if (collision instanceof BlockCastResult.BlockRayCollision entityCollision) {
                foundCollisions.add(entityCollision);
                continue;
            }

            // Must be an entity collision
            if (++blockCollisionCount == collisionThreshold) {
                break;
            }
        }

        return foundCollisions;
    }

    private RaycastUtils() {}
}
