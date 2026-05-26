package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.Nullable;

final class BlockCollision {
    /**
     * Moves an entity with physics applied (ie checking against blocks)
     * <p>
     * Works by getting all the full blocks that an entity could interact with.
     * All bounding boxes inside the full blocks are checked for collisions with the entity.
     */
    static PhysicsResult handlePhysics(BoundingBox boundingBox,
                                       Vec velocity, Pos entityPosition,
                                       Block.Getter getter,
                                       boolean singleCollision) {
        if (velocity.isZero()) {
            // TODO should return a constant
            return new PhysicsResult(entityPosition, Vec.ZERO, false, false, false, false,
                    velocity, new Point[3], new Shape[3], new Point[3], false, SweepResult.NO_COLLISION);
        }
        return stepPhysics(boundingBox, velocity, entityPosition, getter, singleCollision);
    }

    static @Nullable Entity canPlaceBlockAt(Instance instance, Point blockPos, Block b) {
        for (Entity entity : instance.getNearbyEntities(blockPos, 3)) {
            if (!entity.preventBlockPlacement())
                continue;

            final boolean intersects;
            if (entity instanceof Player) {
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

    private static PhysicsResult stepPhysics(BoundingBox boundingBox,
                                             Vec velocity, Pos entityPosition,
                                             Block.Getter getter, boolean singleCollision) {
        // Allocate once and update values
        SweepResult finalResult = new SweepResult(1 - Vec.EPSILON, 0, 0, 0, null, 0, 0, 0, 0, 0, 0);

        boolean foundCollisionX = false, foundCollisionY = false, foundCollisionZ = false;

        Point[] collidedPoints = new Point[3];
        Shape[] collisionShapes = new Shape[3];
        Point[] collisionShapePositions = new Point[3];

        boolean hasCollided = false;

        PhysicsResult result = computePhysics(boundingBox, velocity, entityPosition, getter, finalResult);
        // Loop until no collisions are found.
        // When collisions are found, the collision axis is set to 0
        // Looping until there are no collisions will allow the entity to move in axis other than the collision axis after a collision.
        while (result.collisionX() || result.collisionY() || result.collisionZ()) {
            // Reset final result
            finalResult.normalX = 0;
            finalResult.normalY = 0;
            finalResult.normalZ = 0;

            if (result.collisionX()) {
                foundCollisionX = true;
                collisionShapes[0] = finalResult.collidedShape;
                collisionShapePositions[0] = new Vec(finalResult.collidedShapeX, finalResult.collidedShapeY, finalResult.collidedShapeZ);
                collidedPoints[0] = new Vec(finalResult.collidedPositionX, finalResult.collidedPositionY, finalResult.collidedPositionZ);
                hasCollided = true;
                if (singleCollision) break;
            } else if (result.collisionZ()) {
                foundCollisionZ = true;
                collisionShapes[2] = finalResult.collidedShape;
                collisionShapePositions[2] = new Vec(finalResult.collidedShapeX, finalResult.collidedShapeY, finalResult.collidedShapeZ);
                collidedPoints[2] = new Vec(finalResult.collidedPositionX, finalResult.collidedPositionY, finalResult.collidedPositionZ);
                hasCollided = true;
                if (singleCollision) break;
            } else if (result.collisionY()) {
                foundCollisionY = true;
                collisionShapes[1] = finalResult.collidedShape;
                collisionShapePositions[1] = new Vec(finalResult.collidedShapeX, finalResult.collidedShapeY, finalResult.collidedShapeZ);
                collidedPoints[1] = new Vec(finalResult.collidedPositionX, finalResult.collidedPositionY, finalResult.collidedPositionZ);
                hasCollided = true;
                if (singleCollision) break;
            }

            // If all axis have had collisions, break
            if (foundCollisionX && foundCollisionY && foundCollisionZ) break;
            // If the entity isn't moving, break
            if (result.newVelocity().isZero()) break;

            finalResult.res = 1 - Vec.EPSILON;
            result = computePhysics(boundingBox, result.newVelocity(), result.newPosition(), getter, finalResult);
        }

        finalResult.res = result.res().res;

        final double newDeltaX = foundCollisionX ? 0 : velocity.x();
        final double newDeltaY = foundCollisionY ? 0 : velocity.y();
        final double newDeltaZ = foundCollisionZ ? 0 : velocity.z();

        return new PhysicsResult(result.newPosition(), new Vec(newDeltaX, newDeltaY, newDeltaZ),
                newDeltaY == 0 && velocity.y() < 0,
                foundCollisionX, foundCollisionY, foundCollisionZ, velocity, collidedPoints, collisionShapes, collisionShapePositions, hasCollided, finalResult);
    }

    private static PhysicsResult computePhysics(BoundingBox boundingBox,
                                                Vec velocity, Pos entityPosition,
                                                Block.Getter getter,
                                                SweepResult finalResult) {
        sweepBlocks(boundingBox, velocity, entityPosition, getter, finalResult);

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
                Vec.ZERO, null, null, null, false, finalResult);
    }

    private static void sweepBlocks(BoundingBox boundingBox,
                                    Vec velocity, Pos entityPosition,
                                    Block.Getter getter,
                                    SweepResult finalResult) {
        final double startX = entityPosition.x();
        final double startY = entityPosition.y();
        final double startZ = entityPosition.z();
        final double endX = startX + velocity.x();
        final double endY = startY + velocity.y();
        final double endZ = startZ + velocity.z();

        final int minX = (int) Math.floor(Math.min(startX, endX) + boundingBox.minX());
        final int minY = (int) Math.floor(Math.min(startY, endY) + boundingBox.minY());
        final int minZ = (int) Math.floor(Math.min(startZ, endZ) + boundingBox.minZ());
        final int maxX = (int) Math.floor(Math.max(startX, endX) + boundingBox.maxX());
        final int maxY = (int) Math.floor(Math.max(startY, endY) + boundingBox.maxY());
        final int maxZ = (int) Math.floor(Math.max(startZ, endZ) + boundingBox.maxZ());

        final int stepX = velocity.x() < 0 ? -1 : 1;
        final int stepY = velocity.y() < 0 ? -1 : 1;
        final int stepZ = velocity.z() < 0 ? -1 : 1;
        final int startBlockX = stepX > 0 ? minX : maxX;
        final int endBlockX = stepX > 0 ? maxX : minX;
        final int startBlockY = stepY > 0 ? minY : maxY;
        final int endBlockY = stepY > 0 ? maxY : minY;
        final int startBlockZ = stepZ > 0 ? minZ : maxZ;
        final int endBlockZ = stepZ > 0 ? maxZ : minZ;

        for (int x = startBlockX; x != endBlockX + stepX; x += stepX) {
            for (int y = startBlockY; y != endBlockY + stepY; y += stepY) {
                for (int z = startBlockZ; z != endBlockZ + stepZ; z += stepZ) {
                    checkBoundingBox(x, y, z, velocity, entityPosition, boundingBox, getter, finalResult);
                }
            }
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
                // we should always check both shapes, so no short-circuit here, to handle properties where the bounding box
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

}
