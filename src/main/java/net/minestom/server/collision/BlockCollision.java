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
        final SweepResult finalResult = new SweepResult(1 - Vec.EPSILON, 0, 0, 0, null, 0, 0, 0, 0, 0, 0);
        final Point[] collidedPoints = new Point[3];
        final Shape[] collisionShapes = new Shape[3];
        final Point[] collisionShapePositions = new Point[3];

        Pos position = entityPosition;
        Vec remaining = velocity;
        // Each sweep advances along `remaining` until the first hit, zeroes
        // the collided axis, then repeats so the entity slides on the others.
        while (true) {
            sweepBlocks(boundingBox, remaining, position, getter, finalResult);
            double dx = finalResult.res * remaining.x();
            double dy = finalResult.res * remaining.y();
            double dz = finalResult.res * remaining.z();
            if (Math.abs(dx) < Vec.EPSILON) dx = 0;
            if (Math.abs(dy) < Vec.EPSILON) dy = 0;
            if (Math.abs(dz) < Vec.EPSILON) dz = 0;
            position = position.add(dx, dy, dz);

            // The slab method records the entry face as a single non-zero normal.
            final int axis;
            if (finalResult.normalX != 0) axis = 0;
            else if (finalResult.normalY != 0) axis = 1;
            else if (finalResult.normalZ != 0) axis = 2;
            else break; // no collision this pass

            collisionShapes[axis] = finalResult.collidedShape;
            collisionShapePositions[axis] = new Vec(finalResult.collidedShapeX, finalResult.collidedShapeY, finalResult.collidedShapeZ);
            collidedPoints[axis] = new Vec(finalResult.collidedPositionX, finalResult.collidedPositionY, finalResult.collidedPositionZ);

            if (singleCollision || (collisionShapes[0] != null && collisionShapes[1] != null && collisionShapes[2] != null))
                break;

            remaining = new Vec(
                    axis == 0 ? 0 : remaining.x() - dx,
                    axis == 1 ? 0 : remaining.y() - dy,
                    axis == 2 ? 0 : remaining.z() - dz);
            if (remaining.isZero()) break;

            finalResult.normalX = 0;
            finalResult.normalY = 0;
            finalResult.normalZ = 0;
            finalResult.res = 1 - Vec.EPSILON;
        }

        final boolean foundX = collisionShapes[0] != null;
        final boolean foundY = collisionShapes[1] != null;
        final boolean foundZ = collisionShapes[2] != null;
        final Vec newDelta = new Vec(
                foundX ? 0 : velocity.x(),
                foundY ? 0 : velocity.y(),
                foundZ ? 0 : velocity.z());
        return new PhysicsResult(position, newDelta,
                foundY && velocity.y() < 0,
                foundX, foundY, foundZ,
                velocity, collidedPoints, collisionShapes, collisionShapePositions,
                foundX || foundY || foundZ, finalResult);
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

        // Block-aligned bounds of the swept AABB.
        final int minX = (int) Math.floor(Math.min(startX, endX) + boundingBox.minX());
        final int minY = (int) Math.floor(Math.min(startY, endY) + boundingBox.minY());
        final int minZ = (int) Math.floor(Math.min(startZ, endZ) + boundingBox.minZ());
        final int maxX = (int) Math.floor(Math.max(startX, endX) + boundingBox.maxX());
        final int maxY = (int) Math.floor(Math.max(startY, endY) + boundingBox.maxY());
        final int maxZ = (int) Math.floor(Math.max(startZ, endZ) + boundingBox.maxZ());

        // Walk from near to far along velocity so finalResult.res tightens early
        // (later iterations reject more candidates via `percentage <= res`).
        final int stepX = velocity.x() < 0 ? -1 : 1;
        final int stepY = velocity.y() < 0 ? -1 : 1;
        final int stepZ = velocity.z() < 0 ? -1 : 1;
        final int firstX = stepX > 0 ? minX : maxX, lastX = stepX > 0 ? maxX : minX;
        final int firstY = stepY > 0 ? minY : maxY, lastY = stepY > 0 ? maxY : minY;
        final int firstZ = stepZ > 0 ? minZ : maxZ, lastZ = stepZ > 0 ? maxZ : minZ;

        for (int x = firstX; x != lastX + stepX; x += stepX) {
            for (int y = firstY; y != lastY + stepY; y += stepY) {
                for (int z = firstZ; z != lastZ + stepZ; z += stepZ) {
                    checkBoundingBox(x, y, z, velocity, entityPosition, boundingBox, getter, finalResult);
                }
            }
        }
    }

    /**
     * Check if a moving entity will collide with a block. Updates finalResult.
     * Handles the "tall block below" case (fences/walls) when the current shape
     * is short enough that an entity could be touching the block below.
     */
    static boolean checkBoundingBox(int blockX, int blockY, int blockZ,
                                    Vec entityVelocity, Pos entityPosition, BoundingBox boundingBox,
                                    Block.Getter getter, SweepResult finalResult) {
        final Block currentBlock = getter.getBlock(blockX, blockY, blockZ, Block.Getter.Condition.TYPE);
        final Shape currentShape = currentBlock.registry().collisionShape();
        final boolean currentCollidable = !currentShape.relativeEnd().isZero();
        final boolean currentShort = currentShape.relativeEnd().y() < 0.5;

        if (currentShort && shouldCheckLower(entityVelocity, entityPosition, blockX, blockY, blockZ)) {
            // Below block could be a tall shape (fence, wall) extending into this cell.
            final Shape belowShape = getter.getBlock(blockX, blockY - 1, blockZ, Block.Getter.Condition.TYPE)
                    .registry().collisionShape();
            // Always test both shapes here so we never miss the tall one even if the short one hits.
            if (belowShape.relativeEnd().y() > 1) {
                final boolean belowHit = belowShape.intersectBoxSwept(entityPosition, entityVelocity,
                        blockX, blockY - 1, blockZ, boundingBox, finalResult);
                final boolean currentHit = currentCollidable && currentShape.intersectBoxSwept(entityPosition, entityVelocity,
                        blockX, blockY, blockZ, boundingBox, finalResult);
                return belowHit | currentHit;
            }
            return currentCollidable && currentShape.intersectBoxSwept(entityPosition, entityVelocity,
                    blockX, blockY, blockZ, boundingBox, finalResult);
        }

        if (currentCollidable && currentShape.intersectBoxSwept(entityPosition, entityVelocity,
                blockX, blockY, blockZ, boundingBox, finalResult)) {
            // If the current collision is sufficiently short, we might also need to collide against the block below.
            if (currentShort) {
                final Shape belowShape = getter.getBlock(blockX, blockY - 1, blockZ, Block.Getter.Condition.TYPE)
                        .registry().collisionShape();
                if (belowShape.relativeEnd().y() > 1) {
                    belowShape.intersectBoxSwept(entityPosition, entityVelocity,
                            blockX, blockY - 1, blockZ, boundingBox, finalResult);
                }
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
        // true if the block is at or below the same height as a line drawn from the entity's position to its final destination
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
