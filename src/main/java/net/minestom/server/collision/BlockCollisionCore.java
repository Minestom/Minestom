package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.WorldBorder;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Pure, framework-free collision core operating solely on {@link Block.Getter}.
 * <p>
 * Holds the block-sweep engine plus the {@link Block.Getter}/{@code applyWorldBorder}/{@code parse*}
 * entry points. The live runtime overloads live in {@link CollisionUtils} and {@link BlockCollision}
 * and delegate here.
 */
final class BlockCollisionCore {
    static final Point[] NO_COLLISION_POINTS = new Point[3];
    static final Shape[] NO_COLLISION_SHAPES = new Shape[3];
    static final Point[] NO_COLLISION_SHAPE_POSITIONS = new Point[3];

    private BlockCollisionCore() {}

    /**
     * Moves a bounding box with physics applied (ie checking against blocks)
     * <p>
     * Works by getting all the full blocks that the bounding box could interact with.
     * All bounding boxes inside the full blocks are checked for collisions with the bounding box.
     */
    static PhysicsResult handlePhysics(Block.Getter getter,
                                       BoundingBox boundingBox,
                                       Pos entityPosition, Vec velocity,
                                       @Nullable PhysicsResult lastPhysicsResult,
                                       boolean singleCollision) {
        if (velocity.isZero()) {
            return new PhysicsResult(entityPosition, Vec.ZERO, false, false, false, false,
                    velocity, NO_COLLISION_POINTS, NO_COLLISION_SHAPES, NO_COLLISION_SHAPE_POSITIONS, false, SweepResult.NO_COLLISION);
        }
        // Fast-exit using cache
        final PhysicsResult cachedResult = cachedPhysics(velocity, entityPosition, getter, lastPhysicsResult);
        if (cachedResult != null) {
            return cachedResult;
        }
        // Expensive AABB computation
        return stepPhysics(boundingBox, velocity, entityPosition, getter, singleCollision);
    }

    /**
     * Simulate the entity's collision physics as if the world had no blocks
     *
     * @param entityPosition the position of the entity
     * @param entityVelocity the velocity of the entity
     * @return the result of physics simulation
     */
    static PhysicsResult blocklessCollision(Pos entityPosition, Vec entityVelocity) {
        return new PhysicsResult(entityPosition.add(entityVelocity), entityVelocity, false,
                false, false, false, entityVelocity, NO_COLLISION_POINTS,
                NO_COLLISION_SHAPES, NO_COLLISION_SHAPE_POSITIONS, false, SweepResult.NO_COLLISION);
    }

    /**
     * Applies world border collision.
     *
     * @param worldBorder     the world border
     * @param currentPosition the current position
     * @param newPosition     the future target position
     * @return the position with the world border collision applied (can be {@code newPosition} if not changed)
     */
    static Pos applyWorldBorder(WorldBorder worldBorder, Pos currentPosition, Pos newPosition) {
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

    static Shape parseCollisionShape(Map<Object, Object> internCache, String shape) {
        final Shape cachedShape = (Shape) internCache.get(shape);
        if (cachedShape != null) return cachedShape;
        final Shape parsedShape = ShapeImpl.parseShapeFromRegistry(shape, (byte) 0);
        internCache.put(shape, parsedShape);
        return (Shape) internCache.computeIfAbsent(parsedShape, k -> parsedShape);
    }

    static Shape parseOcclusionShape(Map<Object, Object> internCache, String shape, boolean occludes, byte lightEmission) {
        record ShapeEntry(String shape, boolean occludes, byte lightEmission) {} // Easy way to Hashcode
        ShapeEntry entry = new ShapeEntry(shape, occludes, lightEmission);
        final Shape cachedShape = (Shape) internCache.get(entry);
        if (cachedShape != null) return cachedShape;
        final Shape parsedShape = occludes ? ShapeImpl.parseShapeFromRegistry(shape, lightEmission) : ShapeImpl.emptyShape(lightEmission);
        internCache.put(entry, parsedShape);
        return (Shape) internCache.computeIfAbsent(parsedShape, k -> parsedShape);
    }

    private static PhysicsResult cachedPhysics(Vec velocity, Pos entityPosition,
                                               Block.Getter getter, PhysicsResult lastPhysicsResult) {
        if (lastPhysicsResult != null && lastPhysicsResult.collisionShapes()[1] instanceof ShapeImpl shape) {
            var currentBlock = getter.getBlock(lastPhysicsResult.collisionShapePositions()[1], Block.Getter.Condition.TYPE);
            var lastBlockBoxes = shape.boundingBoxes();
            var currentBlockBoxes = ((ShapeImpl) currentBlock.registry().collisionShape()).boundingBoxes();

            // Fast exit if entity hasn't moved
            if (lastPhysicsResult.collisionY()
                    && velocity.y() == lastPhysicsResult.originalDelta().y()
                    // Check block below to fast exit gravity
                    && currentBlockBoxes.equals(lastBlockBoxes)
                    && velocity.x() == 0 && velocity.z() == 0
                    && entityPosition.samePoint(lastPhysicsResult.newPosition())
                    && !lastBlockBoxes.isEmpty()) {
                if (lastPhysicsResult.cached()) {
                    return lastPhysicsResult;
                } else {
                    return new PhysicsResult(lastPhysicsResult.newPosition(), lastPhysicsResult.newVelocity(),
                            lastPhysicsResult.isOnGround(), lastPhysicsResult.collisionX(), lastPhysicsResult.collisionY(),
                            lastPhysicsResult.collisionZ(), lastPhysicsResult.originalDelta(), lastPhysicsResult.collisionPoints(),
                            lastPhysicsResult.collisionShapes(), lastPhysicsResult.collisionShapePositions(), lastPhysicsResult.hasCollision(), lastPhysicsResult.res(), true);
                }
            }
        }
        return null;
    }

    private static PhysicsResult stepPhysics(BoundingBox boundingBox,
                                             Vec velocity, Pos entityPosition,
                                             Block.Getter getter, boolean singleCollision) {
        final SweepResult finalResult = new SweepResult(1 - Vec.EPSILON, 0, 0, 0, null, 0, 0, 0, 0, 0, 0);

        // Start as the shared (all-null) arrays; only allocate real ones on the first collision.
        Point[] collidedPoints = NO_COLLISION_POINTS;
        Shape[] collisionShapes = NO_COLLISION_SHAPES;
        Point[] collisionShapePositions = NO_COLLISION_SHAPE_POSITIONS;

        Pos position = entityPosition;
        Vec remaining = velocity;
        // Each sweep advances along `remaining` until the first hit, zeroes the
        // collided axis, then repeats so the entity slides along the others.
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

            if (collisionShapes == NO_COLLISION_SHAPES) {
                collidedPoints = new Point[3];
                collisionShapes = new Shape[3];
                collisionShapePositions = new Point[3];
            }
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
        final boolean anyCollision = foundX || foundY || foundZ;
        final boolean allCollision = foundX && foundY && foundZ;
        final Vec newDelta;
        if (!anyCollision) {
            newDelta = velocity;
        } else if (allCollision) {
            newDelta = Vec.ZERO;
        } else {
            newDelta = new Vec(foundX ? 0 : velocity.x(), foundY ? 0 : velocity.y(), foundZ ? 0 : velocity.z());
        }
        return new PhysicsResult(position, newDelta,
                foundY && velocity.y() < 0,
                foundX, foundY, foundZ,
                velocity, collidedPoints, collisionShapes, collisionShapePositions,
                anyCollision, finalResult);
    }

    /**
     * Iterate the blocks overlapping the swept bounding box (start -> start+velocity), near-to-far
     * along the movement so {@code finalResult.res} tightens early and farther blocks are rejected
     * cheaply by the SweepResult distance gate. Each block is visited exactly once.
     */
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

        // Walk from near to far along velocity.
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
