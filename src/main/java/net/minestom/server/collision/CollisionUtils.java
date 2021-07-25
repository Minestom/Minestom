package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.WorldBorder;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;

public class CollisionUtils {

    /**
     * Moves an entity with physics applied (ie checking against blocks)
     *
     * @param entity the entity to move
     * @return the result of physics simulation
     */
    public static PhysicsResult handlePhysics(@NotNull Entity entity, @NotNull Vec deltaPosition) {
        // TODO handle collisions with nearby entities (should it be done here?)
        final Instance instance = entity.getInstance();
        final Chunk originChunk = entity.getChunk();
        final Pos currentPosition = entity.getPosition();
        final BoundingBox boundingBox = entity.getBoundingBox();

        boolean xCollision = false, yCollision = false, zCollision = false;
        double stepSizeX = 0, stepSizeY = 0, stepSizeZ = 0;
        //Reuse array
        double[] displacement = new double[4];

        if (deltaPosition.y() != 0) {
            stepSizeY = stepAxis(instance, originChunk, Axis.Y, deltaPosition.y(),
                    deltaPosition.y() > 0 ? boundingBox.getTopFace() : boundingBox.getBottomFace(), displacement);
            yCollision = stepSizeY != deltaPosition.y();
        }

        if (deltaPosition.x() != 0) {
            stepSizeX = stepAxis(instance, originChunk, Axis.X, deltaPosition.x(),
                    deltaPosition.x() < 0 ? boundingBox.getLeftFace() : boundingBox.getRightFace(), displacement);
            xCollision = stepSizeX != deltaPosition.x();
        }

        if (deltaPosition.z() != 0) {
            stepSizeZ = stepAxis(instance, originChunk, Axis.Z, deltaPosition.z(),
                    deltaPosition.z() > 0 ? boundingBox.getBackFace() : boundingBox.getFrontFace(), displacement);
            zCollision = stepSizeZ != deltaPosition.z();
        }

        return new PhysicsResult(currentPosition.add(stepSizeX, stepSizeY, stepSizeZ),
                new Vec(xCollision ? 0 : deltaPosition.x(),
                        yCollision ? 0 : deltaPosition.y(),
                        zCollision ? 0 : deltaPosition.z()),
                yCollision && deltaPosition.y() < 0);
    }

    /**
     * Steps on a single axis. Checks against collisions for each point of 'corners'. This method assumes that startPosition is valid.
     * Immediately return false if corners is of length 0.
     *
     * @param instance   instance to check blocks from
     * @param axis       step direction. Works best if unit vector and aligned to an axis
     * @param stepAmount how much to step in the direction (in blocks)
     * @param corners    the corners to check against
     * @return maximum step before collision
     */
    private static double stepAxis(final Instance instance, final Chunk originChunk, final Axis axis, final double stepAmount, final Vec[] corners, final double[] displacement) {
        if (corners.length == 0)
            return stepAmount; // avoid degeneracy in following computations

        final double sign = Math.signum(stepAmount);
        int blockLength = (int) stepAmount;
        final double remainingLength = stepAmount - blockLength;
        blockLength = Math.abs(blockLength);

        // used to determine if 'remainingLength' should be used
        boolean collisionFound = false;

        double totalStep = 0;
        for (int i = 0; i < blockLength; i++) {
            final double possibleStep = stepOnce(instance, originChunk, axis, sign, corners, sign, displacement);
            totalStep += possibleStep;
            if (possibleStep != sign) {
                collisionFound = true;
                break;
            }
        }

        // add remainingLength
        if (!collisionFound) {
            totalStep += stepOnce(instance, originChunk, axis, remainingLength, corners, sign, displacement);
        }

        return totalStep;
    }

    /**
     * Steps no more than 1 block once on the given axis.
     *
     * @param instance instance to get blocks from
     * @param axis     the axis to move along
     * @param corners  the corners of the bounding box to consider
     * @return the maximum distance without collision
     */
    private static double stepOnce(final Instance instance, final Chunk originChunk, final Axis axis, final double amount, final Vec[] corners, final double signum, final double[] displacement) {
        // Step each corner
        for (int cornerIndex = 0; cornerIndex < corners.length; cornerIndex++) {
            final Vec originalCorner = corners[cornerIndex];
            // New corner without colliding with something
            final Point newCorner = axis.with.apply(originalCorner, o -> o + amount);

            final Chunk chunk = ChunkUtils.retrieve(instance, originChunk, newCorner);
            // TODO: block collision boxes, for the moment, always consider a full block
            // Check for collision
            if (!ChunkUtils.isLoaded(chunk) /*collision on chunk border*/ ||
                    (chunk.getBlock(newCorner).isSolid()) /*collision with a solid block*/) {
                displacement[cornerIndex] = (axis.get.get(originalCorner) - Math.floor(axis.get.get(newCorner))) + signum == -1 ? 1 : 0;
                if (displacement[cornerIndex] == 0) {
                    // There won't be anything closer to zero
                    return 0;
                }
                continue;
            }
            // No collision, entire step is possible
            displacement[cornerIndex] = amount;
        }
        if (signum < 0) {
            return Arrays.stream(displacement).max().orElse(0);
        } else {
            return Arrays.stream(displacement).min().orElse(0);
        }
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

    enum Axis {
        X(Point::x, Point::withX),
        Y(Point::y, Point::withY),
        Z(Point::z, Point::withZ);

        final Get get;
        final With with;

        Axis(Get get, With with) {
            this.get = get;
            this.with = with;
        }
    }

    @FunctionalInterface
    private interface Get {
        double get(Point point);
    }

    @FunctionalInterface
    private interface With {
        Point apply(Point point, DoubleUnaryOperator operator);
    }
}
