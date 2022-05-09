package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class BlockCollision {
    // Minimum move amount, minimum final velocity
    private static final double MIN_DELTA = 0.001;

    private static Vec[] calculateFaces(Vec queryVec, BoundingBox boundingBox) {
        // Add 1 because we start at point 0
        int ceilX = (int) Math.ceil(boundingBox.width()) + 1;
        int ceilY = (int) Math.ceil(boundingBox.height()) + 1;
        int ceilZ = (int) Math.ceil(boundingBox.depth()) + 1;

        int pointCount = 0;
        if (queryVec.x() != 0) pointCount += ceilY * ceilZ;
        if (queryVec.y() != 0) pointCount += ceilX * ceilZ;
        if (queryVec.z() != 0) pointCount += ceilX * ceilY;

        // Three edge reduction
        if (queryVec.x() != 0 && queryVec.y() != 0 && queryVec.z() != 0) {
            pointCount -= ceilX + ceilY + ceilZ;

            // inclusion exclusion principle
            pointCount++;
        } else if (queryVec.x() != 0 && queryVec.y() != 0) { // Two edge reduction
            pointCount -= ceilZ;
        } else if (queryVec.y() != 0 && queryVec.z() != 0) { // Two edge reduction
            pointCount -= ceilX;
        } else if (queryVec.x() != 0 && queryVec.z() != 0) { // Two edge reduction
            pointCount -= ceilY;
        }

        Vec[] facePoints = new Vec[pointCount];
        int insertIndex = 0;

        // X -> Y x Z
        if (queryVec.x() != 0) {
            int startIOffset = 0, endIOffset = 0, startJOffset = 0, endJOffset = 0;

            // Y handles XY edge
            if (queryVec.y() < 0) startJOffset = 1;
            if (queryVec.y() > 0) endJOffset = 1;

            // Z handles XZ edge
            if (queryVec.z() < 0) startIOffset = 1;
            if (queryVec.z() > 0) endIOffset = 1;

            for (int i = startIOffset; i <= Math.ceil(boundingBox.depth()) - endIOffset; ++i)
                for (int j = startJOffset; j <= Math.ceil(boundingBox.height()) - endJOffset; ++j) {
                    double cellI = i;
                    double cellJ = j;
                    double cellK = queryVec.x() < 0 ? 0 : boundingBox.width();

                    if (i >= boundingBox.depth()) cellI = boundingBox.depth();
                    if (j >= boundingBox.height()) cellJ = boundingBox.height();

                    cellI += boundingBox.minZ();
                    cellJ += boundingBox.minY();
                    cellK += boundingBox.minX();

                    Vec p = new Vec(cellK, cellJ, cellI);
                    facePoints[insertIndex++] = p;
                }
        }

        // Y -> X x Z
        if (queryVec.y() != 0) {
            int startJOffset = 0, endJOffset = 0;

            // Z handles YZ edge
            if (queryVec.z() < 0) startJOffset = 1;
            if (queryVec.z() > 0) endJOffset = 1;

            for (int i = startJOffset; i <= Math.ceil(boundingBox.depth()) - endJOffset; ++i)
                for (int j = 0; j <= Math.ceil(boundingBox.width()); ++j) {
                    double cellI = i;
                    double cellJ = j;
                    double cellK = queryVec.y() < 0 ? 0 : boundingBox.height();

                    if (i >= boundingBox.depth()) cellI = boundingBox.depth();
                    if (j >= boundingBox.width()) cellJ = boundingBox.width();

                    cellI += boundingBox.minZ();
                    cellJ += boundingBox.minX();
                    cellK += boundingBox.minY();

                    Vec p = new Vec(cellJ, cellK, cellI);
                    facePoints[insertIndex++] = p;
                }
        }

        // Z -> X x Y
        if (queryVec.z() != 0) {
            for (int i = 0; i <= Math.ceil(boundingBox.height()); ++i)
                for (int j = 0; j <= Math.ceil(boundingBox.width()); ++j) {
                    double cellI = i;
                    double cellJ = j;
                    double cellK = queryVec.z() < 0 ? 0 : boundingBox.depth();

                    if (i >= boundingBox.height()) cellI = boundingBox.height();
                    if (j >= boundingBox.width()) cellJ = boundingBox.width();

                    cellI += boundingBox.minY();
                    cellJ += boundingBox.minX();
                    cellK += boundingBox.minZ();

                    Vec p = new Vec(cellJ, cellI, cellK);
                    facePoints[insertIndex++] = p;
                }
        }

        return facePoints;
    }

    /**
     * Moves an entity with physics applied (ie checking against blocks)
     * <p>
     * Works by getting all the full blocks that an entity could interact with.
     * All bounding boxes inside the full blocks are checked for collisions with the entity.
     */
    static PhysicsResult handlePhysics(@NotNull BoundingBox boundingBox,
                                       @NotNull Vec entityVelocity, @NotNull Pos entityPosition,
                                       @NotNull Block.Getter getter,
                                       @Nullable PhysicsResult lastPhysicsResult) {
        Vec remainingMove = entityVelocity;

        // Allocate once and update values
        final SweepResult finalResult = new SweepResult(1, 0, 0, 0, null);

        boolean foundCollisionX = false, foundCollisionY = false, foundCollisionZ = false;

        Point collisionYBlock = null;
        Block blockYType = Block.AIR;

        // Check cache to see if the entity is standing on a block without moving.
        // If the entity isn't moving and the block below hasn't changed, return
        if (lastPhysicsResult != null) {
            if (lastPhysicsResult.collisionY()
                    && Math.signum(remainingMove.y()) == Math.signum(lastPhysicsResult.originalDelta().y())
                    && lastPhysicsResult.collidedBlockY() != null
                    && getter.getBlock(lastPhysicsResult.collidedBlockY(), Block.Getter.Condition.TYPE) == lastPhysicsResult.blockTypeY()
                    && remainingMove.x() == 0 && remainingMove.z() == 0
                    && entityPosition.samePoint(lastPhysicsResult.newPosition())
                    && lastPhysicsResult.blockTypeY() != Block.AIR) {
                remainingMove = remainingMove.withY(0);
                foundCollisionY = true;
                collisionYBlock = lastPhysicsResult.collidedBlockY();
                blockYType = lastPhysicsResult.blockTypeY();
            }
        }

        // If we're moving less than the MIN_DELTA value, set the velocity in that axis to 0.
        // This prevents tiny moves from wasting cpu time
        final double deltaX = Math.abs(remainingMove.x()) < MIN_DELTA ? 0 : remainingMove.x();
        final double deltaY = Math.abs(remainingMove.y()) < MIN_DELTA ? 0 : remainingMove.y();
        final double deltaZ = Math.abs(remainingMove.z()) < MIN_DELTA ? 0 : remainingMove.z();

        remainingMove = new Vec(deltaX, deltaY, deltaZ);

        if (remainingMove.isZero())
            if (lastPhysicsResult != null)
                return new PhysicsResult(entityPosition, Vec.ZERO, lastPhysicsResult.isOnGround(),
                        lastPhysicsResult.collisionX(), lastPhysicsResult.collisionY(), lastPhysicsResult.collisionZ(),
                        entityVelocity, lastPhysicsResult.collidedBlockY(), lastPhysicsResult.blockTypeY());
            else
                return new PhysicsResult(entityPosition, Vec.ZERO, false, false, false, false, entityVelocity, null, Block.AIR);

        // Query faces to get the points needed for collision
        Vec[] allFaces = calculateFaces(new Vec(Math.signum(remainingMove.x()), Math.signum(remainingMove.y()), Math.signum(remainingMove.z())), boundingBox);

        PhysicsResult res = handlePhysics(boundingBox, remainingMove, entityPosition, getter, allFaces, finalResult);

        // Loop until no collisions are found.
        // When collisions are found, the collision axis is set to 0
        // Looping until there are no collisions will allow the entity to move in axis other than the collision axis after a collision.
        while (res.collisionX() || res.collisionY() || res.collisionZ()) {
            // Reset final result
            finalResult.res = 1;
            finalResult.normalX = 0;
            finalResult.normalY = 0;
            finalResult.normalZ = 0;

            if (res.collisionX()) foundCollisionX = true;
            if (res.collisionZ()) foundCollisionZ = true;

            if (res.collisionY()) {
                foundCollisionY = true;

                // If we are only moving in the y-axis
                if (!res.collisionX() && !res.collisionZ() && entityVelocity.x() == 0 && entityVelocity.z() == 0) {
                    collisionYBlock = res.collidedBlockY();
                    blockYType = res.blockTypeY();
                }
            }

            // If all axis have had collisions, break
            if (foundCollisionX && foundCollisionY && foundCollisionZ) break;

            // If the entity isn't moving, break
            if (res.newVelocity().isZero()) break;

            allFaces = calculateFaces(new Vec(Math.signum(remainingMove.x()), Math.signum(remainingMove.y()), Math.signum(remainingMove.z())), boundingBox);

            res = handlePhysics(boundingBox, res.newVelocity(), res.newPosition(), getter, allFaces, finalResult);
        }

        final double newDeltaX = foundCollisionX ? 0 : entityVelocity.x();
        final double newDeltaY = foundCollisionY ? 0 : entityVelocity.y();
        final double newDeltaZ = foundCollisionZ ? 0 : entityVelocity.z();

        return new PhysicsResult(res.newPosition(), new Vec(newDeltaX, newDeltaY, newDeltaZ),
                newDeltaY == 0 && entityVelocity.y() < 0,
                foundCollisionX, foundCollisionY, foundCollisionZ, entityVelocity, collisionYBlock, blockYType);
    }

    private static PhysicsResult handlePhysics(@NotNull BoundingBox boundingBox,
                                               @NotNull Vec deltaPosition, Pos entityPosition,
                                               @NotNull Block.Getter getter,
                                               @NotNull Vec[] allFaces,
                                               @NotNull SweepResult finalResult) {

        double remainingX = deltaPosition.x();
        double remainingY = deltaPosition.y();
        double remainingZ = deltaPosition.z();

        // If the movement is small we don't need to run the expensive ray casting.
        // Positions of move less than one can have hardcoded blocks to check for every direction
        if (deltaPosition.length() < 1) {
            for (Vec point : allFaces) {
                Vec pointBefore = point.add(entityPosition);
                Vec pointAfter = point.add(entityPosition).add(deltaPosition);

                // Entity can pass through up to 4 blocks. Starting block, Two intermediate blocks, and a final block.
                // This means we must check every combination of block movements when an entity moves over an axis.
                // 000, 001, 010, 011, etc.
                // There are 8 of these combinations
                // Checks can be limited by checking if we moved across an axis line

                // Pass through (0, 0, 0)
                checkBoundingBox(pointBefore.blockX(), pointBefore.blockY(), pointBefore.blockZ(), deltaPosition, entityPosition, boundingBox, getter, finalResult);

                if (pointBefore.blockX() != pointAfter.blockX()) {
                    // Pass through (+1, 0, 0)
                    checkBoundingBox(pointAfter.blockX(), pointBefore.blockY(), pointBefore.blockZ(), deltaPosition, entityPosition, boundingBox, getter, finalResult);

                    // Checks for moving through 4 blocks
                    if (pointBefore.blockY() != pointAfter.blockY())
                        // Pass through (+1, +1, 0)
                        checkBoundingBox(pointAfter.blockX(), pointAfter.blockY(), pointBefore.blockZ(), deltaPosition, entityPosition, boundingBox, getter, finalResult);

                    if (pointBefore.blockZ() != pointAfter.blockZ())
                        // Pass through (+1, 0, +1)
                        checkBoundingBox(pointAfter.blockX(), pointBefore.blockY(), pointAfter.blockZ(), deltaPosition, entityPosition, boundingBox, getter, finalResult);
                }

                if (pointBefore.blockY() != pointAfter.blockY()) {
                    // Pass through (0, +1, 0)
                    checkBoundingBox(pointBefore.blockX(), pointAfter.blockY(), pointBefore.blockZ(), deltaPosition, entityPosition, boundingBox, getter, finalResult);

                    // Checks for moving through 4 blocks
                    if (pointBefore.blockZ() != pointAfter.blockZ())
                        // Pass through (0, +1, +1)
                        checkBoundingBox(pointBefore.blockX(), pointAfter.blockY(), pointAfter.blockZ(), deltaPosition, entityPosition, boundingBox, getter, finalResult);
                }

                if (pointBefore.blockZ() != pointAfter.blockZ()) {
                    // Pass through (0, 0, +1)
                    checkBoundingBox(pointBefore.blockX(), pointBefore.blockY(), pointAfter.blockZ(), deltaPosition, entityPosition, boundingBox, getter, finalResult);
                }

                // Pass through (+1, +1, +1)
                if (pointBefore.blockX() != pointAfter.blockX()
                        && pointBefore.blockY() != pointAfter.blockY()
                        && pointBefore.blockZ() != pointAfter.blockZ())
                    checkBoundingBox(pointAfter.blockX(), pointAfter.blockY(), pointAfter.blockZ(), deltaPosition, entityPosition, boundingBox, getter, finalResult);
            }
        } else {
            // When large moves are done we need to ray-cast to find all blocks that could intersect with the movement
            for (Vec point : allFaces) {
                RayUtils.RaycastCollision(deltaPosition, point.add(entityPosition), getter, boundingBox, entityPosition, finalResult);
            }
        }

        double finalX = entityPosition.x() + finalResult.res * remainingX;
        double finalY = entityPosition.y() + finalResult.res * remainingY;
        double finalZ = entityPosition.z() + finalResult.res * remainingZ;

        boolean collisionX = false, collisionY = false, collisionZ = false;

        // Remaining delta
        remainingX -= finalResult.res * remainingX;
        remainingY -= finalResult.res * remainingY;
        remainingZ -= finalResult.res * remainingZ;

        if (finalResult.normalX != 0) {
            collisionX = true;
            remainingX = 0;
        }
        if (finalResult.normalY != 0) {
            collisionY = true;
            remainingY = 0;
        }
        if (finalResult.normalZ != 0) {
            collisionZ = true;
            remainingZ = 0;
        }

        remainingX = Math.abs(remainingX) < MIN_DELTA ? 0 : remainingX;
        remainingY = Math.abs(remainingY) < MIN_DELTA ? 0 : remainingY;
        remainingZ = Math.abs(remainingZ) < MIN_DELTA ? 0 : remainingZ;

        finalX = Math.abs(finalX - entityPosition.x()) < MIN_DELTA ? entityPosition.x() : finalX;
        finalY = Math.abs(finalY - entityPosition.y()) < MIN_DELTA ? entityPosition.y() : finalY;
        finalZ = Math.abs(finalZ - entityPosition.z()) < MIN_DELTA ? entityPosition.z() : finalZ;

        return new PhysicsResult(new Pos(finalX, finalY, finalZ),
                new Vec(remainingX, remainingY, remainingZ), collisionY,
                collisionX, collisionY, collisionZ,
                Vec.ZERO, finalResult.collidedShapePosition, finalResult.blockType);
    }

    static Entity canPlaceBlockAt(Instance instance, Point blockPos, Block b) {
        for (Entity entity : instance.getNearbyEntities(blockPos, 3)) {
            final EntityType type = entity.getEntityType();
            if (type == EntityType.ITEM || type == EntityType.ARROW)
                continue;
            // Marker Armor Stands should not prevent block placement
            if (entity.getEntityMeta() instanceof ArmorStandMeta armorStandMeta && armorStandMeta.isMarker())
                continue;

            final boolean intersects;
            if (type == EntityType.PLAYER) {
                // Ignore spectators
                if (((Player)entity).getGameMode() == GameMode.SPECTATOR)
                    continue;
                // Need to move player slightly away from block we're placing.
                // If player is at block 40 we cannot place a block at block 39 with side length 1 because the block will be in [39, 40]
                // For this reason we subtract a small amount from the player position
                Point playerPos = entity.getPosition().add(entity.getPosition().sub(blockPos).mul(0.0000001));
                intersects = b.registry().collisionShape().intersectBox(playerPos.sub(blockPos), entity.getBoundingBox());
            } else {
                intersects = b.registry().collisionShape().intersectBox(entity.getPosition().sub(blockPos), entity.getBoundingBox());
            }
            if (intersects) return entity;
        }
        return null;
    }

    /**
     * Check if a moving entity will collide with a block. Updates finalResult
     *
     * @param blockX         block x position
     * @param blockY         block y position
     * @param blockZ         block z position
     * @param entityVelocity entity movement vector
     * @param entityPosition entity position
     * @param boundingBox    entity bounding box
     * @param getter         block getter
     * @param finalResult    place to store final result of collision
     * @return true if entity finds collision, other false
     */
    static boolean checkBoundingBox(int blockX, int blockY, int blockZ,
                                    Vec entityVelocity, Pos entityPosition, BoundingBox boundingBox,
                                    Block.Getter getter, SweepResult finalResult) {
        // Don't step if chunk isn't loaded yet
        final Block currentBlock = getter.getBlock(blockX, blockY, blockZ, Block.Getter.Condition.TYPE);
        final Shape currentShape = currentBlock.registry().collisionShape();

        final boolean currentCollidable = !currentShape.relativeEnd().isZero();

        // only consider the block below if current is non-collidable or very short
        if((!currentCollidable || currentShape.relativeEnd().y() < 0.5)) {
            // we need to check below for a tall block (fence, wall, ...)
            final Block belowBlock = getter.getBlock(blockX, blockY - 1, blockZ, Block.Getter.Condition.TYPE);
            final Shape belowShape = belowBlock.registry().collisionShape();

            if(belowShape.relativeEnd().y() > 1) {
                final Vec currentPos = new Vec(blockX, blockY, blockZ);
                final Vec belowPos = new Vec(blockX, blockY - 1, blockZ);

                // we should always check both shapes, so no short-circuit here, to handle cases where the bounding box
                // hits the current solid but misses the tall solid
                return belowShape.intersectBoxSwept(entityPosition, entityVelocity, belowPos, boundingBox, finalResult)
                        | (currentCollidable && currentShape.intersectBoxSwept(entityPosition, entityVelocity,
                        currentPos, boundingBox, finalResult));
            }
        }

        return currentCollidable && currentShape.intersectBoxSwept(entityPosition, entityVelocity,
                new Vec(blockX, blockY, blockZ), boundingBox, finalResult);
    }
}
