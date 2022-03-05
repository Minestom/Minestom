package net.minestom.server.collision;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.WorldBorder;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class CollisionUtils {
    // Minimum move amount, minimum final velocity
    private static final double MIN_DELTA = 0.001;

    /**
     * Moves an entity with physics applied (ie checking against blocks)
     *
     * Works by getting all the full blocks that an entity could interact with.
     * All bounding boxes inside the full blocks are checked for collisions with the entity.
     *
     * @param entity the entity to move
     * @return the result of physics simulation
     */
    public static PhysicsResult handlePhysics(@NotNull Entity entity, @NotNull Vec originaDeltaPosition) {
        final BoundingBox.Faces faces = entity.getBoundingBox().faces();
        Vec deltaPosition = originaDeltaPosition;

        // Allocate once and update values
        final RayUtils.SweepResult finalResult = new RayUtils.SweepResult(1, 0, 0, 0, Pos.ZERO, Block.AIR);
        final RayUtils.SweepResult tempResult = new RayUtils.SweepResult(1, 0, 0, 0, Pos.ZERO, Block.AIR);

        boolean foundCollisionX = false, foundCollisionY = false, foundCollisionZ = false;

        Pos collisionYBlock = Pos.ZERO;
        Block blockYType = Block.AIR;

        // Check cache to see if the entity is standing on a block without moving.
        // If the entity isn't moving and the block below hasn't changed, return
        if (entity.lastPhysicsResult != null && entity.getInstance() != null) {
            if (entity.lastPhysicsResult.collisionY
                    && Math.signum(deltaPosition.y()) == Math.signum(entity.lastPhysicsResult.originalDelta.y())
                    && entity.getInstance().getBlock(entity.lastPhysicsResult.collidedBlockY, Block.Getter.Condition.TYPE) == entity.lastPhysicsResult.blockTypeY
                    && deltaPosition.x() == 0 && deltaPosition.z() == 0
                    && entity.getPosition().samePoint(entity.lastPhysicsResult.newPosition)
                    && entity.lastPhysicsResult.blockTypeY != Block.AIR) {
                deltaPosition = deltaPosition.withY(0);
                foundCollisionY = true;
                collisionYBlock = entity.lastPhysicsResult.collidedBlockY;
                blockYType = entity.lastPhysicsResult.blockTypeY;
            }
        }

        // If we're moving less than the MIN_DELTA value, set the velocity in that axis to 0.
        // This prevents tiny moves from wasting cpu time
        double deltaX = Math.abs(deltaPosition.x()) < MIN_DELTA ? 0 : deltaPosition.x();
        double deltaY = Math.abs(deltaPosition.y()) < MIN_DELTA ? 0 : deltaPosition.y();
        double deltaZ = Math.abs(deltaPosition.z()) < MIN_DELTA ? 0 : deltaPosition.z();

        deltaPosition = new Vec(deltaX, deltaY, deltaZ);
        if (deltaPosition.isZero()) return new PhysicsResult(entity.getPosition(), Vec.ZERO, entity.lastPhysicsResult.isOnGround, entity.lastPhysicsResult.collisionX, entity.lastPhysicsResult.collisionY, entity.lastPhysicsResult.collisionZ, originaDeltaPosition, entity.lastPhysicsResult.collidedBlockY, entity.lastPhysicsResult.blockTypeY);

        // Query faces to get the points needed for collision
        Vec queryVec = new Vec(Math.signum(deltaPosition.x()), Math.signum(deltaPosition.y()), Math.signum(deltaPosition.z()));
        List<Vec> allFaces = faces.query().get(queryVec);

        PhysicsResult res = handlePhysics(entity, deltaPosition, entity.getPosition(), allFaces, finalResult, tempResult);

        // Loop until no collisions are found.
        // When collisions are found, the collision axis is set to 0
        // Looping until there are no collisions will allow the entity to move in axis other than the collision axis after a collision.
        while (res.collisionX || res.collisionY || res.collisionZ) {
            // Reset final result
            finalResult.res = 1;
            finalResult.normalx = 0;
            finalResult.normaly = 0;
            finalResult.normalz = 0;

            if (res.collisionX) foundCollisionX = true;
            if (res.collisionZ) foundCollisionZ = true;

            if (res.collisionY) {
                foundCollisionY = true;

                // If we are only moving in the y-axis
                if (!res.collisionX && !res.collisionZ && originaDeltaPosition.x() == 0 && originaDeltaPosition.z() == 0) {
                    collisionYBlock = res.collidedBlockY;
                    blockYType = res.blockTypeY;
                }
            }

            // If all axis have had collisions, break
            if (foundCollisionX && foundCollisionY && foundCollisionZ) break;

            // If the entity isn't moving, break
            if (res.newVelocity.isZero()) break;

            queryVec = new Vec(Math.signum(deltaPosition.x()), Math.signum(deltaPosition.y()), Math.signum(deltaPosition.z()));
            allFaces = faces.query().get(queryVec);

            res = handlePhysics(entity, res.newVelocity, res.newPosition, allFaces, finalResult, tempResult);
        }

        return new PhysicsResult(res.newPosition, res.newVelocity, res.isOnGround, foundCollisionX, foundCollisionY, foundCollisionZ, originaDeltaPosition, collisionYBlock, blockYType);
    }

    /**
     * Does a physics step until a boundary is found
     * @param entity the entity to move
     * @param deltaPosition the movement vector
     * @param entityPosition the position of the entity
     * @param allFaces point list to use for collision checking
     * @param finalResult place to store final result of collision
     * @param tempResult place to store temporary result of collision
     * @return result of physics calculation
     */
    private static PhysicsResult handlePhysics(@NotNull Entity entity, @NotNull Vec deltaPosition, Pos entityPosition, List<Vec> allFaces, RayUtils.SweepResult finalResult, RayUtils.SweepResult tempResult) {
        final Instance instance = entity.getInstance();
        final Chunk originChunk = entity.getChunk();
        final BoundingBox boundingBox = entity.getBoundingBox();

        double deltaX = deltaPosition.x();
        double deltaY = deltaPosition.y();
        double deltaZ = deltaPosition.z();

        Pos correctedEntityPos = entityPosition.add(0, boundingBox.height() / 2, 0);

        // If the movement is small we don't need to run the expensive ray casting.
        if (deltaPosition.length() < 1) {
            // Go through all points to check. See if the point after the move will be in a new block
            // If the point after is in a new block that new block needs to be checked, otherwise only check the current block
            for (Vec point : allFaces) {
                Vec pointBefore = point.add(entityPosition);
                Vec pointAfter = point.add(entityPosition).add(deltaPosition);

                if (pointBefore.blockX() != pointAfter.blockX()) {
                    CollisionUtils.checkBoundingBox(pointAfter.blockX(), pointBefore.blockY(), pointBefore.blockZ(), instance, originChunk, deltaPosition, correctedEntityPos, boundingBox, entityPosition, tempResult, finalResult);

                    if (pointBefore.blockY() != pointAfter.blockY()) {
                        CollisionUtils.checkBoundingBox(pointAfter.blockX(), pointAfter.blockY(), pointBefore.blockZ(), instance, originChunk, deltaPosition, correctedEntityPos, boundingBox, entityPosition, tempResult, finalResult);
                    }
                    if (pointBefore.blockZ() != pointAfter.blockZ()) {
                        CollisionUtils.checkBoundingBox(pointAfter.blockX(), pointBefore.blockY(), pointAfter.blockZ(), instance, originChunk, deltaPosition, correctedEntityPos, boundingBox, entityPosition, tempResult, finalResult);
                    }
                }

                if (pointBefore.blockY() != pointAfter.blockY()) {
                    CollisionUtils.checkBoundingBox(pointBefore.blockX(), pointAfter.blockY(), pointBefore.blockZ(), instance, originChunk, deltaPosition, correctedEntityPos, boundingBox, entityPosition, tempResult, finalResult);

                    if (pointBefore.blockZ() != pointAfter.blockZ()) {
                        CollisionUtils.checkBoundingBox(pointBefore.blockX(), pointAfter.blockY(), pointAfter.blockZ(), instance, originChunk, deltaPosition, correctedEntityPos, boundingBox, entityPosition, tempResult, finalResult);
                    }
                }

                if (pointBefore.blockZ() != pointAfter.blockZ()) {
                    CollisionUtils.checkBoundingBox(pointBefore.blockX(), pointBefore.blockY(), pointAfter.blockZ(), instance, originChunk, deltaPosition, correctedEntityPos, boundingBox, entityPosition, tempResult, finalResult);
                }

                CollisionUtils.checkBoundingBox(pointBefore.blockX(), pointBefore.blockY(), pointBefore.blockZ(), instance, originChunk, deltaPosition, correctedEntityPos, boundingBox, entityPosition, tempResult, finalResult);

                if (pointBefore.blockX() != pointAfter.blockX()
                        && pointBefore.blockY() != pointAfter.blockY()
                        && pointBefore.blockZ() != pointAfter.blockZ()
                )
                    CollisionUtils.checkBoundingBox(pointAfter.blockX(), pointAfter.blockY(), pointAfter.blockZ(), instance, originChunk, deltaPosition, correctedEntityPos, boundingBox, entityPosition, tempResult, finalResult);
            }
        } else {
            // When large moves are done we need to raycast to find all blocks that could intersect with the movement
            for (Vec point : allFaces) {
                RayUtils.Raycast(deltaPosition, point.add(entityPosition), instance, originChunk, correctedEntityPos, boundingBox, entityPosition, tempResult, finalResult);
            }
        }

        double finalX = entityPosition.x() + deltaX;
        double finalY = entityPosition.y() + deltaY;
        double finalZ = entityPosition.z() + deltaZ;

        boolean collisionX = false, collisionY = false, collisionZ = false;

        if (finalResult != null) {
            // Update final position
            finalX = entityPosition.x() + finalResult.res * deltaX;
            finalY = entityPosition.y() + finalResult.res * deltaY;
            finalZ = entityPosition.z() + finalResult.res * deltaZ;

            if (finalResult.normalx != 0) {
                collisionX = true;
                deltaX = 0;
            }

            if (finalResult.normaly != 0) {
                collisionY = true;
                deltaY = 0;
            }

            if (finalResult.normalz != 0) {
                collisionZ = true;
                deltaZ = 0;
            }
        }

        deltaX = Math.abs(deltaX) < MIN_DELTA ? 0 : deltaX;
        deltaY = Math.abs(deltaY) < MIN_DELTA ? 0 : deltaY;
        deltaZ = Math.abs(deltaZ) < MIN_DELTA ? 0 : deltaZ;

        finalX = Math.abs(finalX - entityPosition.x()) < MIN_DELTA ? entityPosition.x() : finalX;
        finalY = Math.abs(finalY - entityPosition.y()) < MIN_DELTA ? entityPosition.y() : finalY;
        finalZ = Math.abs(finalZ - entityPosition.z()) < MIN_DELTA ? entityPosition.z() : finalZ;

        return new PhysicsResult(new Pos(finalX, finalY, finalZ), new Vec(deltaX, deltaY, deltaZ), Math.abs(deltaY) <= MIN_DELTA, collisionX, collisionY, collisionZ, Vec.ZERO, finalResult.collisionBlock, finalResult.blockType);
    }

    /**
     * Check if a moving entity will collide with a block. Updates finalResult
     * @param blockX block x position
     * @param blockY block y position
     * @param blockZ block z position
     * @param instance entity instance
     * @param originChunk entity chunk
     * @param deltaPosition entity movement vector
     * @param correctedEntityPos entity position from centre. By default entity position is from the bottom
     * @param boundingBox entity bounding box
     * @param entityPosition entity position
     * @param finalResult place to store final result of collision
     * @param tempResult place to store temporary result of collision
     * @return true if entity finds collision, other false
     */
    public static boolean checkBoundingBox(int blockX, int blockY, int blockZ, Instance instance, Chunk originChunk, Vec deltaPosition, Pos correctedEntityPos, BoundingBox boundingBox, Pos entityPosition, RayUtils.SweepResult tempResult, RayUtils.SweepResult finalResult) {
        final Chunk c = ChunkUtils.retrieve(instance, originChunk, blockX, blockZ);
        // Don't step if chunk isn't loaded yet
        Block checkBlock = !ChunkUtils.isLoaded(c) ? Block.STONE : c.getBlock(blockX, blockY, blockZ, Block.Getter.Condition.TYPE);

        boolean hitBlock = false;

        if (checkBlock.isSolid()) {
            for (int i = 0; i < checkBlock.registry().boundingBoxes().length; ++i) {
                BoundingBox bb = checkBlock.registry().boundingBoxes()[i];

                // Fast check to see if a collision happens
                // Uses minkowski sum
                boolean hasCollision = RayUtils.RayBoundingBoxIntersectCheck(
                        deltaPosition,
                        bb,
                        correctedEntityPos,
                        blockX, blockY, blockZ,
                        boundingBox.width(),
                        boundingBox.height(),
                        boundingBox.depth());

                if (!hasCollision) continue;

                // Longer check to get result of collision
                RayUtils.SweptAABB(boundingBox, bb, entityPosition, blockX, blockY, blockZ, deltaPosition.x(), deltaPosition.y(), deltaPosition.z(), tempResult);

                // Update final result if the temp result collision is sooner than the current final result
                if (tempResult.res < finalResult.res) {
                    finalResult.res = tempResult.res;
                    finalResult.normalx = tempResult.normalx;
                    finalResult.normaly = tempResult.normaly;
                    finalResult.normalz = tempResult.normalz;
                    finalResult.collisionBlock = new Pos(blockX, blockY, blockZ);
                    finalResult.blockType = checkBlock;
                }

                hitBlock = true;
            }
        }

        return hitBlock;
    }

    /**
     * Applies world border collision.
     *
     * @param instance        the instance where the world border is
     * @param currentPosition the current position
     * @param newPosition     the future target position
     * @return the position with the world border collision applied (can be {@code newPosition} if not changed)
     */
    public static @NotNull Pos applyWorldBorder(@NotNull Instance instance,
                                                @NotNull Pos currentPosition, @NotNull Pos newPosition) {
        final WorldBorder worldBorder = instance.getWorldBorder();
        final WorldBorder.CollisionAxis collisionAxis = worldBorder.getCollisionAxis(newPosition);
        return switch (collisionAxis) {
            case NONE ->
                    // Apply velocity + gravity
                    newPosition;
            case BOTH ->
                    // Apply Y velocity/gravity
                    new Pos(currentPosition.x(), newPosition.y(), currentPosition.z());
            case X ->
                    // Apply Y/Z velocity/gravity
                    new Pos(currentPosition.x(), newPosition.y(), newPosition.z());
            case Z ->
                    // Apply X/Y velocity/gravity
                    new Pos(newPosition.x(), newPosition.y(), currentPosition.z());
        };
    }

    public record PhysicsResult(Pos newPosition, Vec newVelocity, boolean isOnGround, boolean collisionX, boolean collisionY, boolean collisionZ, Vec originalDelta, Pos collidedBlockY, Block blockTypeY) {
    }
}
