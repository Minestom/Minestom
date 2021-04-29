package net.minestom.server.collision;

import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.WorldBorder;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.Vector;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;

public class CollisionUtils {

    private static final Vector Y_AXIS = new Vector(0, 1, 0);
    private static final Vector X_AXIS = new Vector(1, 0, 0);
    private static final Vector Z_AXIS = new Vector(0, 0, 1);

    /**
     * Moves an entity with physics applied (ie checking against blocks)
     *
     * @param entity        the entity to move
     * @param deltaPosition
     * @param positionOut   the Position object in which the new position will be saved
     * @param velocityOut   the Vector object in which the new velocity will be saved
     * @return whether this entity is on the ground
     */
    public static boolean handlePhysics(@NotNull Entity entity,
                                        @NotNull Vector deltaPosition,
                                        @NotNull Position positionOut,
                                        @NotNull Vector velocityOut) {
        // TODO handle collisions with nearby entities (should it be done here?)
        final Instance instance = entity.getInstance();
        final Chunk originChunk = entity.getChunk();
        final Position currentPosition = entity.getPosition();
        final BoundingBox boundingBox = entity.getBoundingBox();

        Vector intermediaryPosition = new Vector();
        boolean yCollision = stepAxis(instance, originChunk, currentPosition.toVector(), Y_AXIS, deltaPosition.getY(),
                intermediaryPosition,
                deltaPosition.getY() > 0 ? boundingBox.getTopFace() : boundingBox.getBottomFace());

        boolean xCollision = stepAxis(instance, originChunk, intermediaryPosition, X_AXIS, deltaPosition.getX(),
                intermediaryPosition,
                deltaPosition.getX() < 0 ? boundingBox.getLeftFace() : boundingBox.getRightFace());

        boolean zCollision = stepAxis(instance, originChunk, intermediaryPosition, Z_AXIS, deltaPosition.getZ(),
                intermediaryPosition,
                deltaPosition.getZ() > 0 ? boundingBox.getBackFace() : boundingBox.getFrontFace());

        positionOut.setX(intermediaryPosition.getX());
        positionOut.setY(intermediaryPosition.getY());
        positionOut.setZ(intermediaryPosition.getZ());
        velocityOut.copy(deltaPosition);
        if (xCollision) {
            velocityOut.setX(0f);
        }
        if (yCollision) {
            velocityOut.setY(0f);
        }
        if (zCollision) {
            velocityOut.setZ(0f);
        }

        return yCollision && deltaPosition.getY() < 0;
    }

    /**
     * Steps on a single axis. Checks against collisions for each point of 'corners'. This method assumes that startPosition is valid.
     * Immediately return false if corners is of length 0.
     *
     * @param instance      instance to check blocks from
     * @param startPosition starting position for stepping, can be intermediary position from last step
     * @param axis          step direction. Works best if unit vector and aligned to an axis
     * @param stepAmount    how much to step in the direction (in blocks)
     * @param positionOut   the vector in which to store the new position
     * @param corners       the corners to check against
     * @return true if a collision has been found
     */
    private static boolean stepAxis(Instance instance,
                                    Chunk originChunk,
                                    Vector startPosition, Vector axis,
                                    double stepAmount, Vector positionOut,
                                    Vector... corners) {
        positionOut.copy(startPosition);
        if (corners.length == 0)
            return false; // avoid degeneracy in following computations
        // perform copies to allow in place modifications
        // prevents making a lot of new objects. Well at least it reduces the count
        BlockPosition[] cornerPositions = new BlockPosition[corners.length];
        Vector[] cornersCopy = new Vector[corners.length];
        for (int i = 0; i < corners.length; i++) {
            cornersCopy[i] = corners[i].clone();
            cornerPositions[i] = new BlockPosition(corners[i]);
        }

        final double sign = Math.signum(stepAmount);
        final int blockLength = (int) stepAmount;
        final double remainingLength = stepAmount - blockLength;
        // used to determine if 'remainingLength' should be used
        boolean collisionFound = false;
        for (int i = 0; i < Math.abs(blockLength); i++) {
            if (!stepOnce(instance, originChunk, axis, sign, cornersCopy, cornerPositions)) {
                collisionFound = true;
            }
            if (collisionFound) {
                break;
            }
        }

        // add remainingLength
        if (!collisionFound) {
            Vector direction = new Vector();
            direction.copy(axis);
            collisionFound = !stepOnce(instance, originChunk, direction, remainingLength, cornersCopy, cornerPositions);
        }

        // find the corner which moved the least
        double smallestDisplacement = Double.POSITIVE_INFINITY;
        for (int i = 0; i < corners.length; i++) {
            final double displacement = corners[i].distance(cornersCopy[i]);
            if (displacement < smallestDisplacement) {
                smallestDisplacement = displacement;
            }
        }

        positionOut.copy(startPosition);
        positionOut.add(smallestDisplacement * axis.getX() * sign, smallestDisplacement * axis.getY() * sign, smallestDisplacement * axis.getZ() * sign);
        return collisionFound;
    }

    /**
     * Steps once (by a length of 1 block) on the given axis.
     *
     * @param instance        instance to get blocks from
     * @param axis            the axis to move along
     * @param amount
     * @param cornersCopy     the corners of the bounding box to consider (mutable)
     * @param cornerPositions the corners, converted to BlockPosition (mutable)
     * @return false if this method encountered a collision
     */
    private static boolean stepOnce(Instance instance,
                                    Chunk originChunk,
                                    Vector axis, double amount, Vector[] cornersCopy, BlockPosition[] cornerPositions) {
        final double sign = Math.signum(amount);
        for (int cornerIndex = 0; cornerIndex < cornersCopy.length; cornerIndex++) {
            Vector corner = cornersCopy[cornerIndex];
            BlockPosition blockPos = cornerPositions[cornerIndex];
            corner.add(axis.getX() * amount, axis.getY() * amount, axis.getZ() * amount);
            blockPos.setX((int) Math.floor(corner.getX()));
            blockPos.setY((int) Math.floor(corner.getY()));
            blockPos.setZ((int) Math.floor(corner.getZ()));

            Chunk chunk = originChunk;
            if (!ChunkUtils.same(originChunk, blockPos.getX(), blockPos.getZ())) {
                chunk = instance.getChunkAt(blockPos);
                if (!ChunkUtils.isLoaded(chunk)) {
                    // Collision at chunk border
                    return false;
                }
            }

            final short blockStateId = chunk.getBlockStateId(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            final Block block = Block.fromStateId(blockStateId);

            // TODO: block collision boxes
            // TODO: for the moment, always consider a full block
            if (block.isSolid()) {
                corner.subtract(axis.getX() * amount, axis.getY() * amount, axis.getZ() * amount);

                if (Math.abs(axis.getX()) > 10e-16) {
                    corner.setX(blockPos.getX() - axis.getX() * sign);
                }
                if (Math.abs(axis.getY()) > 10e-16) {
                    corner.setY(blockPos.getY() - axis.getY() * sign);
                }
                if (Math.abs(axis.getZ()) > 10e-16) {
                    corner.setZ(blockPos.getZ() - axis.getZ() * sign);
                }

                return false;
            }
        }
        return true;
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
    public static Position applyWorldBorder(@NotNull Instance instance,
                                            @NotNull Position currentPosition, @NotNull Position newPosition) {
        final WorldBorder worldBorder = instance.getWorldBorder();
        final WorldBorder.CollisionAxis collisionAxis = worldBorder.getCollisionAxis(newPosition);
        switch (collisionAxis) {
            case NONE:
                // Apply velocity + gravity
                return newPosition;
            case BOTH:
                // Apply Y velocity/gravity
                return new Position(currentPosition.getX(), newPosition.getY(), currentPosition.getZ());
            case X:
                // Apply Y/Z velocity/gravity
                return new Position(currentPosition.getX(), newPosition.getY(), newPosition.getZ());
            case Z:
                // Apply X/Y velocity/gravity
                return new Position(newPosition.getX(), newPosition.getY(), currentPosition.getZ());
        }
        throw new IllegalStateException("Something weird happened...");
    }

}
