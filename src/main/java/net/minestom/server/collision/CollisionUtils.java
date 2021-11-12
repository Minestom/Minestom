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

import java.util.List;

public class CollisionUtils {

    private static final Vec Y_AXIS = new Vec(0, 1, 0);
    private static final Vec X_AXIS = new Vec(1, 0, 0);
    private static final Vec Z_AXIS = new Vec(0, 0, 1);

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

        Vec stepVec = currentPosition.asVec();
        boolean xCheck = false, yCheck = false, zCheck = false;

        if (deltaPosition.y() != 0) {
            final StepResult yCollision = stepAxis(instance, originChunk, stepVec, Y_AXIS, deltaPosition.y(),
                    deltaPosition.y() > 0 ? boundingBox.getTopFace() : boundingBox.getBottomFace());
            yCheck = yCollision.foundCollision;
            stepVec = yCollision.newPosition;
        }

        if (deltaPosition.x() != 0) {
            final StepResult xCollision = stepAxis(instance, originChunk, stepVec, X_AXIS, deltaPosition.x(),
                    deltaPosition.x() < 0 ? boundingBox.getLeftFace() : boundingBox.getRightFace());
            xCheck = xCollision.foundCollision;
            stepVec = xCollision.newPosition;
        }

        if (deltaPosition.z() != 0) {
            final StepResult zCollision = stepAxis(instance, originChunk, stepVec, Z_AXIS, deltaPosition.z(),
                    deltaPosition.z() > 0 ? boundingBox.getBackFace() : boundingBox.getFrontFace());
            zCheck = zCollision.foundCollision;
            stepVec = zCollision.newPosition;
        }

        return new PhysicsResult(currentPosition.samePoint(stepVec) ? currentPosition : currentPosition.withCoord(stepVec),
                new Vec(xCheck ? 0 : deltaPosition.x(),
                        yCheck ? 0 : deltaPosition.y(),
                        zCheck ? 0 : deltaPosition.z()),
                yCheck && deltaPosition.y() < 0);
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
    private static StepResult stepAxis(Instance instance, Chunk originChunk, Vec startPosition, Vec axis, double stepAmount, List<Vec> corners) {
        final Vec[] mutableCorners = corners.toArray(Vec[]::new);
        final double sign = Math.signum(stepAmount);
        final int blockLength = (int) stepAmount;
        final double remainingLength = stepAmount - blockLength;
        // used to determine if 'remainingLength' should be used
        boolean collisionFound = false;
        for (int i = 0; i < Math.abs(blockLength); i++) {
            collisionFound = stepOnce(instance, originChunk, axis, sign, mutableCorners);
            if (collisionFound) break;
        }

        // add remainingLength
        if (!collisionFound) {
            collisionFound = stepOnce(instance, originChunk, axis, remainingLength, mutableCorners);
        }

        // find the corner which moved the least
        double smallestDisplacement = Double.POSITIVE_INFINITY;
        for (int i = 0; i < corners.size(); i++) {
            final double displacement = corners.get(i).distance(mutableCorners[i]);
            if (displacement < smallestDisplacement) {
                smallestDisplacement = displacement;
            }
        }

        return new StepResult(startPosition.add(new Vec(smallestDisplacement).mul(axis).mul(sign)), collisionFound);
    }

    /**
     * Steps once (by a length of 1 block) on the given axis.
     *
     * @param instance instance to get blocks from
     * @param axis     the axis to move along
     * @param corners  the corners of the bounding box to consider
     * @return true if found collision
     */
    private static boolean stepOnce(Instance instance, Chunk originChunk, Vec axis, double amount, Vec[] corners) {
        final double sign = Math.signum(amount);
        for (int cornerIndex = 0; cornerIndex < corners.length; cornerIndex++) {
            final Vec originalCorner = corners[cornerIndex];
            final Vec newCorner = originalCorner.add(axis.mul(amount));
            final Chunk chunk = ChunkUtils.retrieve(instance, originChunk, newCorner);
            if (!ChunkUtils.isLoaded(chunk)) {
                // Collision at chunk border
                return true;
            }
            final Block block = chunk.getBlock(newCorner, Block.Getter.Condition.TYPE);
            // TODO: block collision boxes
            // TODO: for the moment, always consider a full block
            if (block != null && block.isSolid()) {
                corners[cornerIndex] = new Vec(
                        Math.abs(axis.x()) > 10e-16 ? newCorner.blockX() - axis.x() * sign : originalCorner.x(),
                        Math.abs(axis.y()) > 10e-16 ? newCorner.blockY() - axis.y() * sign : originalCorner.y(),
                        Math.abs(axis.z()) > 10e-16 ? newCorner.blockZ() - axis.z() * sign : originalCorner.z());
                return true;
            }
            corners[cornerIndex] = newCorner;
        }
        return false;
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

    public record PhysicsResult(Pos newPosition,
                                Vec newVelocity,
                                boolean isOnGround) {
    }

    private record StepResult(Vec newPosition,
                              boolean foundCollision) {
    }
}
