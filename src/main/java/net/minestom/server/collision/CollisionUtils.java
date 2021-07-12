package net.minestom.server.collision;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.WorldBorder;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockGetter;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;

import java.util.function.DoubleUnaryOperator;

public class CollisionUtils {
    /**
     * Moves an entity with physics applied (ie checking against blocks)
     *
     * @param entity        the entity to move
     * @return the result of physics simulation
     */
    public static PhysicsResult handlePhysics(@NotNull Entity entity, @NotNull Vec deltaPosition) {

        // TODO handle collisions with nearby entities (should it be done here?)
        final Instance instance = entity.getInstance();
        final Chunk originChunk = entity.getChunk();
        final Pos currentPosition = entity.getPosition();
        final BoundingBox boundingBox = entity.getBoundingBox();

        StepResult stepResult = new StepResult(currentPosition, false);
        final boolean xCollision, yCollision, zCollision;

        if (deltaPosition.y() != 0) {
            stepResult = stepAxis(instance, originChunk, stepResult.newPosition, Axis.Y, deltaPosition.y(),
                    deltaPosition.y() > 0 ? boundingBox.getTopFace() : boundingBox.getBottomFace());
            yCollision = stepResult.foundCollision;
        } else {
            yCollision = false;
        }

        if (deltaPosition.x() != 0) {
            stepResult = stepAxis(instance, originChunk, stepResult.newPosition, Axis.X, deltaPosition.x(),
                    deltaPosition.x() < 0 ? boundingBox.getLeftFace() : boundingBox.getRightFace());
            xCollision = stepResult.foundCollision;
        } else {
            xCollision = false;
        }

        if (deltaPosition.z() != 0) {
            stepResult = stepAxis(instance, originChunk, stepResult.newPosition, Axis.Z, deltaPosition.z(),
                    deltaPosition.z() > 0 ? boundingBox.getBackFace() : boundingBox.getFrontFace());
            zCollision = stepResult.foundCollision;
        } else {
            zCollision = false;
        }

        return new PhysicsResult(stepResult.newPosition,
                deltaPosition.x() == 0 && deltaPosition.z() == 0 && (yCollision || deltaPosition.y() == 0) ? Vec.ZERO :
                deltaPosition.apply(((x, y, z) -> new Vec(
                        xCollision ? 0 : x,
                        yCollision ? 0 : y,
                        zCollision ? 0 : z
                ))), yCollision && deltaPosition.y() < 0);
    }

    /**
     * Steps on a single axis. Checks against collisions for each point of 'corners'. This method assumes that startPosition is valid.
     * Immediately return false if corners is of length 0.
     *
     * @param instance      instance to check blocks from
     * @param startPosition starting position for stepping, can be intermediary position from last step
     * @param axis          step direction. Works best if unit vector and aligned to an axis
     * @param stepAmount    how much to step in the direction (in blocks)
     * @param corners       the corners to check against
     * @return result of the step
     */
    private static StepResult stepAxis(Instance instance, Chunk originChunk, Pos startPosition, Axis axis, double stepAmount, Vec... corners) {
        if (corners.length == 0)
            return new StepResult(startPosition, false); // avoid degeneracy in following computations

        final Vec[] originalCorners = corners.clone();
        final double sign = Math.signum(stepAmount);
        final int blockLength = (int) stepAmount;
        final double remainingLength = stepAmount - blockLength;
        // used to determine if 'remainingLength' should be used
        boolean collisionFound = false;
        int collidingCornerIndex = 0;
        for (int i = 0; i < Math.abs(blockLength); i++) {
            final int cornerIndex = stepOnce(instance, originChunk, axis, sign, corners);
            if (cornerIndex > -1) {
                collisionFound = true;
                collidingCornerIndex = cornerIndex;
                break;
            }
        }

        // add remainingLength
        if (!collisionFound) {
            final int cornerIndex = stepOnce(instance, originChunk, axis, remainingLength, corners);
            if (cornerIndex > -1) {
                collisionFound = true;
                collidingCornerIndex = cornerIndex;
            }
        }

        int finalCollidingCornerIndex = collidingCornerIndex;
        final DoubleUnaryOperator function = a -> a + originalCorners[finalCollidingCornerIndex].distance(corners[finalCollidingCornerIndex]) * sign;
        switch (axis) {
            case X:
                return new StepResult(startPosition.withX(function), collisionFound);
            case Y:
                return new StepResult(startPosition.withY(function), collisionFound);
            case Z:
                return new StepResult(startPosition.withZ(function), collisionFound);
            default:
                throw new IllegalStateException("Get out of the 4th dimension, this method can only handle three.");
        }
    }

    /**
     * Steps once (by a length of 1 block) on the given axis.
     *
     * @param instance  instance to get blocks from
     * @param axis      the axis to move along
     * @param corners   the corners of the bounding box to consider
     * @return index of colliding corner, -1 if there is none
     */
    private static int stepOnce(Instance instance, Chunk originChunk, Axis axis, double amount, Vec[] corners) {
        final double sign = Math.signum(amount);
        for (int cornerIndex = 0; cornerIndex < corners.length; cornerIndex++) {
            final Vec originalCorner = corners[cornerIndex];
            final Vec newCorner;
            switch (axis) {
                case X:
                    newCorner = originalCorner.withX(a -> a + amount);
                    break;
                case Y:
                    newCorner = originalCorner.withY(a -> a + amount);
                    break;
                case Z:
                    newCorner = originalCorner.withZ(a -> a + amount);
                    break;
                default:
                    throw new IllegalStateException();
            }

            Chunk chunk = ChunkUtils.retrieve(instance, originChunk, newCorner);
            if (!ChunkUtils.isLoaded(chunk)) {
                // Collision at chunk border
                return cornerIndex;
            }

            final Block block = chunk.getBlock(newCorner);

            // TODO: block collision boxes
            // TODO: for the moment, always consider a full block
            if (block.isSolid()) {
                switch (axis) {
                    case X:
                        corners[cornerIndex] = originalCorner.withX(newCorner.blockX() - sign);
                        break;
                    case Y:
                        corners[cornerIndex] = originalCorner.withY(newCorner.blockY() - sign);
                        break;
                    case Z:
                        corners[cornerIndex] = originalCorner.withZ(newCorner.blockZ() - sign);
                        break;
                }
                return cornerIndex;
            }

            corners[cornerIndex] = newCorner;
        }
        return -1;
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
        switch (collisionAxis) {
            case NONE:
                // Apply velocity + gravity
                return newPosition;
            case BOTH:
                // Apply Y velocity/gravity
                return new Pos(currentPosition.x(), newPosition.y(), currentPosition.z());
            case X:
                // Apply Y/Z velocity/gravity
                return new Pos(currentPosition.x(), newPosition.y(), newPosition.z());
            case Z:
                // Apply X/Y velocity/gravity
                return new Pos(newPosition.x(), newPosition.y(), currentPosition.z());
        }
        throw new IllegalStateException("Something weird happened...");
    }

    public static class PhysicsResult {
        private final Pos newPosition;
        private final Vec newVelocity;
        private final boolean isOnGround;

        public PhysicsResult(Pos newPosition, Vec newVelocity, boolean isOnGround) {
            this.newPosition = newPosition;
            this.newVelocity = newVelocity;
            this.isOnGround = isOnGround;
        }

        public Pos newPosition() {
            return newPosition;
        }

        public Vec newVelocity() {
            return newVelocity;
        }

        public boolean isOnGround() {
            return isOnGround;
        }
    }

    private static class StepResult {
        private final Pos newPosition;
        private final boolean foundCollision;

        public StepResult(Pos newPosition, boolean foundCollision) {
            this.newPosition = newPosition;
            this.foundCollision = foundCollision;
        }
    }

    private enum Axis {
        X, Y, Z;
    }
}
