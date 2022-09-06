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
import net.minestom.server.utils.block.BlockIterator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class BlockCollision {
    /**
     * Moves an entity with physics applied (ie checking against blocks)
     * <p>
     * Works by getting all the full blocks that an entity could interact with.
     * All bounding boxes inside the full blocks are checked for collisions with the entity.
     */
    static PhysicsResult handlePhysics(@NotNull BoundingBox boundingBox,
                                       @NotNull Vec velocity, @NotNull Pos entityPosition,
                                       @NotNull Block.Getter getter,
                                       @Nullable PhysicsResult lastPhysicsResult) {
        if (velocity.isZero()) {
            // TODO should return a constant
            return new PhysicsResult(entityPosition, Vec.ZERO, false, false, false, false, velocity, null, Block.AIR);
        }
        // Fast-exit using cache
        final PhysicsResult cachedResult = cachedPhysics(velocity, entityPosition, getter, lastPhysicsResult);
        if (cachedResult != null) {
            return cachedResult;
        }
        // Expensive AABB computation
        return stepPhysics(boundingBox, velocity, entityPosition, getter);
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
            if (entity instanceof Player) {
                // Ignore spectators
                if (((Player) entity).getGameMode() == GameMode.SPECTATOR)
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

    private static PhysicsResult cachedPhysics(Vec velocity, Pos entityPosition,
                                               Block.Getter getter, PhysicsResult lastPhysicsResult) {
        if (lastPhysicsResult != null) {
            if (lastPhysicsResult.collisionY()
                    && velocity.y() == lastPhysicsResult.originalDelta().y()
                    && lastPhysicsResult.collidedBlockY() != null
                    && getter.getBlock(lastPhysicsResult.collidedBlockY(), Block.Getter.Condition.TYPE) == lastPhysicsResult.blockTypeY()
                    && velocity.x() == 0 && velocity.z() == 0
                    && entityPosition.samePoint(lastPhysicsResult.newPosition())
                    && lastPhysicsResult.blockTypeY() != Block.AIR) {
                return lastPhysicsResult;
            }
        }
        return null;
    }


    private static PhysicsResult stepPhysics(@NotNull BoundingBox boundingBox,
                                             @NotNull Vec velocity, @NotNull Pos entityPosition,
                                             @NotNull Block.Getter getter) {
        // Allocate once and update values
        SweepResult finalResult = new SweepResult(1 - Vec.EPSILON, 0, 0, 0, null);

        boolean foundCollisionX = false, foundCollisionY = false, foundCollisionZ = false;
        Point collisionYBlock = null;
        Block blockYType = Block.AIR;

        // Query faces to get the points needed for collision
        final Vec[] allFaces = calculateFaces(velocity, boundingBox);
        PhysicsResult result = computePhysics(boundingBox, velocity, entityPosition, getter, allFaces, finalResult);
        // Loop until no collisions are found.
        // When collisions are found, the collision axis is set to 0
        // Looping until there are no collisions will allow the entity to move in axis other than the collision axis after a collision.
        while (result.collisionX() || result.collisionY() || result.collisionZ()) {
            // Reset final result
            finalResult.res = 1 - Vec.EPSILON;
            finalResult.normalX = 0;
            finalResult.normalY = 0;
            finalResult.normalZ = 0;

            if (result.collisionX()) foundCollisionX = true;
            if (result.collisionZ()) foundCollisionZ = true;
            if (result.collisionY()) {
                foundCollisionY = true;
                // If we are only moving in the y-axis
                if (!result.collisionX() && !result.collisionZ() && velocity.x() == 0 && velocity.z() == 0) {
                    collisionYBlock = result.collidedBlockY();
                    blockYType = result.blockTypeY();
                }
            }
            // If all axis have had collisions, break
            if (foundCollisionX && foundCollisionY && foundCollisionZ) break;
            // If the entity isn't moving, break
            if (result.newVelocity().isZero()) break;

            result = computePhysics(boundingBox, result.newVelocity(), result.newPosition(), getter, allFaces, finalResult);
        }

        final double newDeltaX = foundCollisionX ? 0 : velocity.x();
        final double newDeltaY = foundCollisionY ? 0 : velocity.y();
        final double newDeltaZ = foundCollisionZ ? 0 : velocity.z();

        return new PhysicsResult(result.newPosition(), new Vec(newDeltaX, newDeltaY, newDeltaZ),
                newDeltaY == 0 && velocity.y() < 0,
                foundCollisionX, foundCollisionY, foundCollisionZ, velocity, collisionYBlock, blockYType);
    }

    private static PhysicsResult computePhysics(@NotNull BoundingBox boundingBox,
                                                @NotNull Vec velocity, Pos entityPosition,
                                                @NotNull Block.Getter getter,
                                                @NotNull Vec[] allFaces,
                                                @NotNull SweepResult finalResult) {
        // If the movement is small we don't need to run the expensive ray casting.
        // Positions of move less than one can have hardcoded blocks to check for every direction
        if (velocity.length() < 1) {
            fastPhysics(boundingBox, velocity, entityPosition, getter, allFaces, finalResult);
        } else {
            slowPhysics(boundingBox, velocity, entityPosition, getter, allFaces, finalResult);
        }

        final boolean collisionX = finalResult.normalX != 0;
        final boolean collisionY = finalResult.normalY != 0;
        final boolean collisionZ = finalResult.normalZ != 0;

        double deltaX = finalResult.res * velocity.x();
        double deltaY = finalResult.res * velocity.y();
        double deltaZ = finalResult.res * velocity.z();

        if (Math.abs(deltaX) < Vec.EPSILON) deltaX = 0;
        if (Math.abs(deltaY) < Vec.EPSILON) deltaY = 0;
        if (Math.abs(deltaZ) < Vec.EPSILON) deltaZ = 0;

        final Pos finalPos = entityPosition.add(deltaX, deltaY, deltaZ);

        final double remainingX = collisionX ? 0 : velocity.x() - deltaX;
        final double remainingY = collisionY ? 0 : velocity.y() - deltaY;
        final double remainingZ = collisionZ ? 0 : velocity.z() - deltaZ;

        return new PhysicsResult(finalPos, new Vec(remainingX, remainingY, remainingZ),
                collisionY, collisionX, collisionY, collisionZ,
                Vec.ZERO, finalResult.collidedShapePosition, finalResult.blockType);
    }

    private static void slowPhysics(@NotNull BoundingBox boundingBox,
                                    @NotNull Vec velocity, Pos entityPosition,
                                    @NotNull Block.Getter getter,
                                    @NotNull Vec[] allFaces,
                                    @NotNull SweepResult finalResult) {
        // When large moves are done we need to ray-cast to find all blocks that could intersect with the movement
        for (Vec point : allFaces) {
            BlockIterator iterator = new BlockIterator(Vec.fromPoint(point.add(entityPosition)), velocity, 0, (int) Math.ceil(velocity.length()));
            while (iterator.hasNext()) {
                Point p = iterator.next();
                // sqrt 3 (1.733) is the maximum error
                if (Vec.fromPoint(p.sub(entityPosition)).length() > (finalResult.res * velocity.length() + 1.733))
                    break;
                if (checkBoundingBox(p.blockX(), p.blockY(), p.blockZ(), velocity, entityPosition, boundingBox, getter, finalResult))
                    break;
            }
        }
    }

    private static void fastPhysics(@NotNull BoundingBox boundingBox,
                                    @NotNull Vec velocity, Pos entityPosition,
                                    @NotNull Block.Getter getter,
                                    @NotNull Vec[] allFaces,
                                    @NotNull SweepResult finalResult) {
        for (Vec point : allFaces) {
            final Vec pointBefore = point.add(entityPosition);
            final Vec pointAfter = point.add(entityPosition).add(velocity);
            // Entity can pass through up to 4 blocks. Starting block, Two intermediate blocks, and a final block.
            // This means we must check every combination of block movements when an entity moves over an axis.
            // 000, 001, 010, 011, etc.
            // There are 8 of these combinations
            // Checks can be limited by checking if we moved across an axis line

            // Pass through (0, 0, 0)
            checkBoundingBox(pointBefore.blockX(), pointBefore.blockY(), pointBefore.blockZ(), velocity, entityPosition, boundingBox, getter, finalResult);

            if (pointBefore.blockX() != pointAfter.blockX()) {
                // Pass through (+1, 0, 0)
                checkBoundingBox(pointAfter.blockX(), pointBefore.blockY(), pointBefore.blockZ(), velocity, entityPosition, boundingBox, getter, finalResult);

                // Checks for moving through 4 blocks
                if (pointBefore.blockY() != pointAfter.blockY())
                    // Pass through (+1, +1, 0)
                    checkBoundingBox(pointAfter.blockX(), pointAfter.blockY(), pointBefore.blockZ(), velocity, entityPosition, boundingBox, getter, finalResult);

                if (pointBefore.blockZ() != pointAfter.blockZ())
                    // Pass through (+1, 0, +1)
                    checkBoundingBox(pointAfter.blockX(), pointBefore.blockY(), pointAfter.blockZ(), velocity, entityPosition, boundingBox, getter, finalResult);
            }

            if (pointBefore.blockY() != pointAfter.blockY()) {
                // Pass through (0, +1, 0)
                checkBoundingBox(pointBefore.blockX(), pointAfter.blockY(), pointBefore.blockZ(), velocity, entityPosition, boundingBox, getter, finalResult);

                // Checks for moving through 4 blocks
                if (pointBefore.blockZ() != pointAfter.blockZ())
                    // Pass through (0, +1, +1)
                    checkBoundingBox(pointBefore.blockX(), pointAfter.blockY(), pointAfter.blockZ(), velocity, entityPosition, boundingBox, getter, finalResult);
            }

            if (pointBefore.blockZ() != pointAfter.blockZ()) {
                // Pass through (0, 0, +1)
                checkBoundingBox(pointBefore.blockX(), pointBefore.blockY(), pointAfter.blockZ(), velocity, entityPosition, boundingBox, getter, finalResult);
            }

            // Pass through (+1, +1, +1)
            if (pointBefore.blockX() != pointAfter.blockX()
                    && pointBefore.blockY() != pointAfter.blockY()
                    && pointBefore.blockZ() != pointAfter.blockZ())
                checkBoundingBox(pointAfter.blockX(), pointAfter.blockY(), pointAfter.blockZ(), velocity, entityPosition, boundingBox, getter, finalResult);
        }
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
        final boolean currentShort = currentShape.relativeEnd().y() < 0.5;

        // only consider the block below if our current shape is sufficiently short
        if (currentShort && shouldCheckLower(entityVelocity, entityPosition, blockX, blockY, blockZ)) {
            // we need to check below for a tall block (fence, wall, ...)
            final Vec belowPos = new Vec(blockX, blockY - 1, blockZ);
            final Block belowBlock = getter.getBlock(belowPos, Block.Getter.Condition.TYPE);
            final Shape belowShape = belowBlock.registry().collisionShape();

            final Vec currentPos = new Vec(blockX, blockY, blockZ);
            // don't fall out of if statement, we could end up redundantly grabbing a block, and we only need to
            // collision check against the current shape since the below shape isn't tall
            if (belowShape.relativeEnd().y() > 1) {
                // we should always check both shapes, so no short-circuit here, to handle cases where the bounding box
                // hits the current solid but misses the tall solid
                return belowShape.intersectBoxSwept(entityPosition, entityVelocity, belowPos, boundingBox, finalResult) |
                        (currentCollidable && currentShape.intersectBoxSwept(entityPosition, entityVelocity, currentPos, boundingBox, finalResult));
            } else {
                return currentCollidable && currentShape.intersectBoxSwept(entityPosition, entityVelocity, currentPos, boundingBox, finalResult);
            }
        }

        if (currentCollidable && currentShape.intersectBoxSwept(entityPosition, entityVelocity,
                new Vec(blockX, blockY, blockZ), boundingBox, finalResult)) {
            // if the current collision is sufficiently short, we might need to collide against the block below too
            if (currentShort) {
                final Vec belowPos = new Vec(blockX, blockY - 1, blockZ);
                final Block belowBlock = getter.getBlock(belowPos, Block.Getter.Condition.TYPE);
                final Shape belowShape = belowBlock.registry().collisionShape();
                // only do sweep if the below block is big enough to possibly hit
                if (belowShape.relativeEnd().y() > 1)
                    belowShape.intersectBoxSwept(entityPosition, entityVelocity, belowPos, boundingBox, finalResult);
            }
            return true;
        }
        return false;
    }

    private static boolean shouldCheckLower(Vec entityVelocity, Pos entityPosition, int blockX, int blockY, int blockZ) {
        final double yVelocity = entityVelocity.y();
        // if moving horizontally, just check if the floor of the entity's position is the same as the blockY
        if (yVelocity == 0) return Math.floor(entityPosition.y()) == blockY;
        final double xVelocity = entityVelocity.x();
        final double zVelocity = entityVelocity.z();
        // if moving straight up, don't bother checking for tall solids beneath anything
        // if moving straight down, only check for a tall solid underneath the last block
        if (xVelocity == 0 && zVelocity == 0)
            return yVelocity < 0 && blockY == Math.floor(entityPosition.y() + yVelocity);
        // default to true: if no x velocity, only consider YZ line, and vice-versa
        final boolean underYX = xVelocity != 0 && computeHeight(yVelocity, xVelocity, entityPosition.y(), entityPosition.x(), blockX) >= blockY;
        final boolean underYZ = zVelocity != 0 && computeHeight(yVelocity, zVelocity, entityPosition.y(), entityPosition.z(), blockZ) >= blockY;
        // true if the block is at or below the same height as a line drawn from the entity's position to its final
        // destination
        return underYX && underYZ;
    }

    /*
    computes the height of the entity at the given block position along a projection of the line it's travelling along
    (YX or YZ). the returned value will be greater than or equal to the block height if the block is along the lower
    layer of intersections with this line.
     */
    private static double computeHeight(double yVelocity, double velocity, double entityY, double pos, int blockPos) {
        final double m = yVelocity / velocity;
        /*
        offsetting by 1 is necessary with a positive slope, because we can clip the bottom-right corner of blocks
        without clipping the "bottom-left" (the smallest corner of the block on the YZ or YX plane). without the offset
        these would not be considered to be on the lowest layer, since our block position represents the bottom-left
        corner
         */
        return m * (blockPos - pos + (m > 0 ? 1 : 0)) + entityY;
    }

    private static Vec[] calculateFaces(Vec queryVec, BoundingBox boundingBox) {
        final int queryX = (int) Math.signum(queryVec.x());
        final int queryY = (int) Math.signum(queryVec.y());
        final int queryZ = (int) Math.signum(queryVec.z());

        final int ceilWidth = (int) Math.ceil(boundingBox.width());
        final int ceilHeight = (int) Math.ceil(boundingBox.height());
        final int ceilDepth = (int) Math.ceil(boundingBox.depth());
        Vec[] facePoints;
        // Compute array length
        {
            final int ceilX = ceilWidth + 1;
            final int ceilY = ceilHeight + 1;
            final int ceilZ = ceilDepth + 1;
            int pointCount = 0;
            if (queryX != 0) pointCount += ceilY * ceilZ;
            if (queryY != 0) pointCount += ceilX * ceilZ;
            if (queryZ != 0) pointCount += ceilX * ceilY;
            // Three edge reduction
            if (queryX != 0 && queryY != 0 && queryZ != 0) {
                pointCount -= ceilX + ceilY + ceilZ;
                // inclusion exclusion principle
                pointCount++;
            } else if (queryX != 0 && queryY != 0) { // Two edge reduction
                pointCount -= ceilZ;
            } else if (queryY != 0 && queryZ != 0) { // Two edge reduction
                pointCount -= ceilX;
            } else if (queryX != 0 && queryZ != 0) { // Two edge reduction
                pointCount -= ceilY;
            }
            facePoints = new Vec[pointCount];
        }
        int insertIndex = 0;
        // X -> Y x Z
        if (queryX != 0) {
            int startIOffset = 0, endIOffset = 0, startJOffset = 0, endJOffset = 0;
            // Y handles XY edge
            if (queryY < 0) startJOffset = 1;
            if (queryY > 0) endJOffset = 1;
            // Z handles XZ edge
            if (queryZ < 0) startIOffset = 1;
            if (queryZ > 0) endIOffset = 1;

            for (int i = startIOffset; i <= ceilDepth - endIOffset; ++i) {
                for (int j = startJOffset; j <= ceilHeight - endJOffset; ++j) {
                    double cellI = i;
                    double cellJ = j;
                    double cellK = queryX < 0 ? 0 : boundingBox.width();

                    if (i >= boundingBox.depth()) cellI = boundingBox.depth();
                    if (j >= boundingBox.height()) cellJ = boundingBox.height();

                    cellI += boundingBox.minZ();
                    cellJ += boundingBox.minY();
                    cellK += boundingBox.minX();

                    facePoints[insertIndex++] = new Vec(cellK, cellJ, cellI);
                }
            }
        }
        // Y -> X x Z
        if (queryY != 0) {
            int startJOffset = 0, endJOffset = 0;
            // Z handles YZ edge
            if (queryZ < 0) startJOffset = 1;
            if (queryZ > 0) endJOffset = 1;

            for (int i = startJOffset; i <= ceilDepth - endJOffset; ++i) {
                for (int j = 0; j <= ceilWidth; ++j) {
                    double cellI = i;
                    double cellJ = j;
                    double cellK = queryY < 0 ? 0 : boundingBox.height();

                    if (i >= boundingBox.depth()) cellI = boundingBox.depth();
                    if (j >= boundingBox.width()) cellJ = boundingBox.width();

                    cellI += boundingBox.minZ();
                    cellJ += boundingBox.minX();
                    cellK += boundingBox.minY();

                    facePoints[insertIndex++] = new Vec(cellJ, cellK, cellI);
                }
            }
        }
        // Z -> X x Y
        if (queryZ != 0) {
            for (int i = 0; i <= ceilHeight; ++i) {
                for (int j = 0; j <= ceilWidth; ++j) {
                    double cellI = i;
                    double cellJ = j;
                    double cellK = queryZ < 0 ? 0 : boundingBox.depth();

                    if (i >= boundingBox.height()) cellI = boundingBox.height();
                    if (j >= boundingBox.width()) cellJ = boundingBox.width();

                    cellI += boundingBox.minY();
                    cellJ += boundingBox.minX();
                    cellK += boundingBox.minZ();

                    facePoints[insertIndex++] = new Vec(cellJ, cellI, cellK);
                }
            }
        }

        return facePoints;
    }
}
