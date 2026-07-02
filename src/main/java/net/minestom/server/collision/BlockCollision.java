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
    static final Point[] NO_COLLISION_POINTS = new Point[3];
    static final Shape[] NO_COLLISION_SHAPES = new Shape[3];
    static final Point[] NO_COLLISION_SHAPE_POSITIONS = new Point[3];

    /**
     * Moves an entity with physics applied (ie checking against blocks)
     * <p>
     * Works by getting all the full blocks that an entity could interact with.
     * All bounding boxes inside the full blocks are checked for collisions with the entity.
     */
    static PhysicsResult handlePhysics(BoundingBox boundingBox,
                                       Vec velocity, Pos entityPosition,
                                       Block.Getter getter,
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

    static Entity canPlaceBlockAt(Instance instance, Point blockPos, Block b) {
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
        final Sweep sweep = new Sweep(boundingBox, getter, finalResult);

        // Start as the shared (all-null) arrays; only allocate real ones on the first collision.
        Point[] collidedPoints = NO_COLLISION_POINTS;
        Shape[] collisionShapes = NO_COLLISION_SHAPES;
        Point[] collisionShapePositions = NO_COLLISION_SHAPE_POSITIONS;

        Pos position = entityPosition;
        Vec remaining = velocity;
        // Each sweep advances along `remaining` until the first hit, zeroes the
        // collided axis, then repeats so the entity slides along the others.
        while (true) {
            sweep.sweepBlocks(position, remaining);
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
     * One block-sweep pass: primitive mover state shared by the per-block checks, refreshed at the
     * start of each pass. Keeps the hot loop free of {@link Point} math and argument plumbing.
     */
    private static final class Sweep {
        final BoundingBox boundingBox;
        final Block.Getter getter;
        final SweepResult result;
        double posX, posY, posZ; // entity position
        double vX, vY, vZ; // movement this pass
        double moMinX, moMinY, moMinZ, moMaxX, moMaxY, moMaxZ; // mover world bounds
        // One-slot block memo; valid across passes since blocks don't change mid-simulation
        int cacheX, cacheY, cacheZ;
        @Nullable Block cachedBlock;

        Sweep(BoundingBox boundingBox, Block.Getter getter, SweepResult result) {
            this.boundingBox = boundingBox;
            this.getter = getter;
            this.result = result;
        }

        /**
         * Iterate the blocks overlapping the swept bounding box (start -> start+velocity), near-to-far
         * along the movement so {@code result.res} tightens early and farther blocks are rejected
         * cheaply by the SweepResult distance gate. Each block is visited exactly once.
         */
        void sweepBlocks(Pos position, Vec velocity) {
            posX = position.x();
            posY = position.y();
            posZ = position.z();
            vX = velocity.x();
            vY = velocity.y();
            vZ = velocity.z();
            moMinX = posX + boundingBox.minX();
            moMinY = posY + boundingBox.minY();
            moMinZ = posZ + boundingBox.minZ();
            moMaxX = posX + boundingBox.maxX();
            moMaxY = posY + boundingBox.maxY();
            moMaxZ = posZ + boundingBox.maxZ();

            // Block-aligned bounds of the swept AABB.
            final int minX = (int) Math.floor(Math.min(posX, posX + vX) + boundingBox.minX());
            final int minY = (int) Math.floor(Math.min(posY, posY + vY) + boundingBox.minY());
            final int minZ = (int) Math.floor(Math.min(posZ, posZ + vZ) + boundingBox.minZ());
            final int maxX = (int) Math.floor(Math.max(posX, posX + vX) + boundingBox.maxX());
            final int maxY = (int) Math.floor(Math.max(posY, posY + vY) + boundingBox.maxY());
            final int maxZ = (int) Math.floor(Math.max(posZ, posZ + vZ) + boundingBox.maxZ());

            // Walk from near to far along velocity.
            final int stepX = vX < 0 ? -1 : 1;
            final int stepY = vY < 0 ? -1 : 1;
            final int stepZ = vZ < 0 ? -1 : 1;
            final int firstX = stepX > 0 ? minX : maxX, lastX = stepX > 0 ? maxX : minX;
            final int firstY = stepY > 0 ? minY : maxY, lastY = stepY > 0 ? maxY : minY;
            final int firstZ = stepZ > 0 ? minZ : maxZ, lastZ = stepZ > 0 ? maxZ : minZ;

            // Y innermost so the one-slot block memo dedups the below-block lookups of short blocks
            // against the column walk (the below block of one step is the current block of the next).
            for (int x = firstX; x != lastX + stepX; x += stepX) {
                for (int z = firstZ; z != lastZ + stepZ; z += stepZ) {
                    for (int y = firstY; y != lastY + stepY; y += stepY) {
                        checkBlock(x, y, z);
                    }
                }
            }

            // Collided position of the winning hit, filled once per pass.
            if (result.normalX != 0 || result.normalY != 0 || result.normalZ != 0) {
                result.collidedPositionX = posX + vX * result.res;
                result.collidedPositionY = posY + vY * result.res;
                result.collidedPositionZ = posZ + vZ * result.res;
            }
        }

        /**
         * Check if the moving entity will collide with the block at the given position (and, for
         * short blocks, the potentially tall block below it). Updates {@code result}.
         */
        private Block blockAt(int x, int y, int z) {
            final Block cached = cachedBlock;
            if (cached != null && x == cacheX && y == cacheY && z == cacheZ) return cached;
            final Block block = getter.getBlock(x, y, z, Block.Getter.Condition.TYPE);
            cacheX = x;
            cacheY = y;
            cacheZ = z;
            cachedBlock = block;
            return block;
        }

        boolean checkBlock(int blockX, int blockY, int blockZ) {
            // Don't step if chunk isn't loaded yet
            final Block currentBlock = blockAt(blockX, blockY, blockZ);
            final Shape currentShape = currentBlock.registry().collisionShape();

            final boolean currentCollidable = !currentShape.relativeEnd().isZero();
            final boolean currentShort = currentShape.relativeEnd().y() < 0.5;

            // only consider the block below if our current shape is sufficiently short
            if (currentShort && shouldCheckLower(blockX, blockY, blockZ)) {
                // we need to check below for a tall block (fence, wall, ...)
                final Block belowBlock = blockAt(blockX, blockY - 1, blockZ);
                final Shape belowShape = belowBlock.registry().collisionShape();

                // don't fall out of if statement, we could end up redundantly grabbing a block, and we only need to
                // collision check against the current shape since the below shape isn't tall
                if (belowShape.relativeEnd().y() > 1) {
                    // we should always check both shapes, so no short-circuit here, to handle properties where the bounding box
                    // hits the current solid but misses the tall solid
                    return sweepShape(belowShape, blockX, blockY - 1, blockZ) |
                            (currentCollidable && sweepShape(currentShape, blockX, blockY, blockZ));
                } else {
                    return currentCollidable && sweepShape(currentShape, blockX, blockY, blockZ);
                }
            }

            if (currentCollidable && sweepShape(currentShape, blockX, blockY, blockZ)) {
                // if the current collision is sufficiently short, we might need to collide against the block below too
                if (currentShort) {
                    final Block belowBlock = blockAt(blockX, blockY - 1, blockZ);
                    final Shape belowShape = belowBlock.registry().collisionShape();
                    // only do sweep if the below block is big enough to possibly hit
                    if (belowShape.relativeEnd().y() > 1)
                        sweepShape(belowShape, blockX, blockY - 1, blockZ);
                }
                return true;
            }
            return false;
        }

        private boolean sweepShape(Shape shape, int blockX, int blockY, int blockZ) {
            if (shape instanceof ShapeImpl impl)
                return impl.sweep(moMinX, moMinY, moMinZ, moMaxX, moMaxY, moMaxZ,
                        vX, vY, vZ, blockX, blockY, blockZ, result);
            // Custom Shape implementations go through the public API
            return shape.intersectBoxSwept(new Pos(posX, posY, posZ), new Vec(vX, vY, vZ),
                    new Vec(blockX, blockY, blockZ), boundingBox, result);
        }

        private boolean shouldCheckLower(int blockX, int blockY, int blockZ) {
            // if moving horizontally, just check if the floor of the entity's position is the same as the blockY
            if (vY == 0) return Math.floor(posY) == blockY;
            // if moving straight up, don't bother checking for tall solids beneath anything
            // if moving straight down, only check for a tall solid underneath the last block
            if (vX == 0 && vZ == 0)
                return vY < 0 && blockY == Math.floor(posY + vY);
            // default to true: if no x velocity, only consider YZ line, and vice-versa
            final boolean underYX = vX != 0 && computeHeight(vY, vX, posY, posX, blockX) >= blockY;
            final boolean underYZ = vZ != 0 && computeHeight(vY, vZ, posY, posZ, blockZ) >= blockY;
            // true if the block is at or below the same height as a line drawn from the entity's position to its final
            // destination
            return underYX && underYZ;
        }
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
