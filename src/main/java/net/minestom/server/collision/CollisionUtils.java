package net.minestom.server.collision;

import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.WorldBorder;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.coordinate.Point;
import net.minestom.server.utils.coordinate.Pos;
import net.minestom.server.utils.coordinate.Vec;
import org.jetbrains.annotations.NotNull;

public class CollisionUtils {

    private static final Vec Y_AXIS = new Vec(0, 1, 0);
    private static final Vec X_AXIS = new Vec(1, 0, 0);
    private static final Vec Z_AXIS = new Vec(0, 0, 1);

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

        final StepResult yCollision = stepAxis(instance, originChunk, currentPosition.asVec(), Y_AXIS, deltaPosition.y(),
                deltaPosition.y() > 0 ? boundingBox.getTopFace() : boundingBox.getBottomFace());

        final StepResult xCollision = stepAxis(instance, originChunk, yCollision.newPosition, X_AXIS, deltaPosition.x(),
                deltaPosition.x() < 0 ? boundingBox.getLeftFace() : boundingBox.getRightFace());

        final StepResult zCollision = stepAxis(instance, originChunk, xCollision.newPosition, Z_AXIS, deltaPosition.z(),
                deltaPosition.z() > 0 ? boundingBox.getBackFace() : boundingBox.getFrontFace());

        return new PhysicsResult(currentPosition.withCoord(zCollision.newPosition),
                deltaPosition.with(((x, y, z) -> new Vec(
                        xCollision.foundCollision ? 0 : x,
                        yCollision.foundCollision ? 0 : y,
                        zCollision.foundCollision ? 0 : z
                ))), yCollision.foundCollision && deltaPosition.y() < 0);
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
    private static StepResult stepAxis(Instance instance, Chunk originChunk, Vec startPosition, Vec axis, double stepAmount, Vec... corners) {
        if (corners.length == 0)
            return new StepResult(startPosition, false); // avoid degeneracy in following computations

        final Vec[] originalCorners = corners.clone();
        final double sign = Math.signum(stepAmount);
        final int blockLength = (int) stepAmount;
        final double remainingLength = stepAmount - blockLength;
        // used to determine if 'remainingLength' should be used
        boolean collisionFound = false;
        for (int i = 0; i < Math.abs(blockLength); i++) {
            final OneStepResult oneStepResult = stepOnce(instance, originChunk, axis, sign, corners);
            corners = oneStepResult.newCorners;
            if (collisionFound = oneStepResult.foundCollision) break;
        }

        // add remainingLength
        if (!collisionFound) {
            final OneStepResult oneStepResult = stepOnce(instance, originChunk, axis, remainingLength, corners);
            corners = oneStepResult.newCorners;
            collisionFound = oneStepResult.foundCollision;
        }

        // find the corner which moved the least
        double smallestDisplacement = Double.POSITIVE_INFINITY;
        for (int i = 0; i < corners.length; i++) {
            final double displacement = originalCorners[i].distance(corners[i]);
            if (displacement < smallestDisplacement) {
                smallestDisplacement = displacement;
            }
        }

        return new StepResult(startPosition.add(new Vec(smallestDisplacement).mul(axis).mul(sign)), collisionFound);
    }

    /**
     * Steps once (by a length of 1 block) on the given axis.
     *
     * @param instance  instance to get blocks from
     * @param axis      the axis to move along
     * @param corners   the corners of the bounding box to consider
     * @return the result of one step
     */
    private static OneStepResult stepOnce(Instance instance, Chunk originChunk, Vec axis, double amount, Vec[] corners) {
        final double sign = Math.signum(amount);
        Vec[] newCorners = new Vec[corners.length];
        for (int cornerIndex = 0; cornerIndex < corners.length; cornerIndex++) {
            final Vec originalCorner = corners[cornerIndex];
            final Vec corner = originalCorner.add(axis.mul(amount));

            Chunk chunk = ChunkUtils.retrieve(instance, originChunk, corner);
            if (!ChunkUtils.isLoaded(chunk)) {
                // Collision at chunk border
                return new OneStepResult(corners, true);
            }

            final Block block = chunk.getBlock(corner);

            // TODO: block collision boxes
            // TODO: for the moment, always consider a full block
            if (block.isSolid()) {
                newCorners[cornerIndex] = originalCorner.with(((x, y, z) -> new Vec(
                        Math.abs(axis.x()) > 10e-16 ? originalCorner.blockX() - axis.x() * sign : x,
                        Math.abs(axis.y()) > 10e-16 ? originalCorner.blockY() - axis.y() * sign : y,
                        Math.abs(axis.z()) > 10e-16 ? originalCorner.blockZ() - axis.z() * sign : z
                )));

                return new OneStepResult(newCorners, true);
            }

            newCorners[cornerIndex] = corner;
        }
        return new OneStepResult(newCorners, false);
    }

    /**
     * Applies world border collision.
     *
     * @param instance        the instance where the world border is
     * @param currentPosition the current position
     * @param newPosition     the future target position
     * @return the position with the world border collision applied (can be {@code newPosition} if not changed)
     */
    @NotNull
    public static Point applyWorldBorder(@NotNull Instance instance,
                                         @NotNull Point currentPosition, @NotNull Point newPosition) {
        final WorldBorder worldBorder = instance.getWorldBorder();
        final WorldBorder.CollisionAxis collisionAxis = worldBorder.getCollisionAxis(newPosition);
        switch (collisionAxis) {
            case NONE:
                // Apply velocity + gravity
                return newPosition;
            case BOTH:
                // Apply Y velocity/gravity
                return new Vec(currentPosition.x(), newPosition.y(), currentPosition.z());
            case X:
                // Apply Y/Z velocity/gravity
                return new Vec(currentPosition.x(), newPosition.y(), newPosition.z());
            case Z:
                // Apply X/Y velocity/gravity
                return new Vec(newPosition.x(), newPosition.y(), currentPosition.z());
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

        public Pos getNewPosition() {
            return newPosition;
        }

        public Vec getNewVelocity() {
            return newVelocity;
        }

        public boolean isOnGround() {
            return isOnGround;
        }
    }

    private static class StepResult {
        private final Vec newPosition;
        private final boolean foundCollision;

        public StepResult(Vec newPosition, boolean foundCollision) {
            this.newPosition = newPosition;
            this.foundCollision = foundCollision;
        }
    }

    private static class OneStepResult {
        private final Vec[] newCorners;
        private final boolean foundCollision;

        public OneStepResult(Vec[] newCorners, boolean foundCollision) {
            this.newCorners = newCorners;
            this.foundCollision = foundCollision;
        }
    }
}
