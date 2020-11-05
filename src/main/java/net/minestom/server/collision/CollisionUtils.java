package net.minestom.server.collision;

import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.Vector;
import net.minestom.server.utils.chunk.ChunkUtils;

public class CollisionUtils {

    private static final Vector Y_AXIS = new Vector(0, 1, 0);
    private static final Vector X_AXIS = new Vector(1, 0, 0);
    private static final Vector Z_AXIS = new Vector(0, 0, 1);

    /**
     * Moves an entity with physics applied (ie checking against blocks)
     *
     * @param entity      the entity to move
     * @param positionOut the Position object in which the new position will be saved
     * @param velocityOut the Vector object in which the new velocity will be saved
     * @return whether this entity is on the ground
     */
    public static boolean handlePhysics(Entity entity, Vector deltaPosition, Position positionOut, Vector velocityOut) {
        // TODO handle collisions with nearby entities (should it be done here?)
        final Instance instance = entity.getInstance();
        final Position currentPosition = entity.getPosition();
        final BoundingBox boundingBox = entity.getBoundingBox();

        Vector intermediaryPosition = new Vector();
        final boolean yCollision = stepAxis(instance, currentPosition.toVector(), Y_AXIS, deltaPosition.getY(),
                intermediaryPosition,
                deltaPosition.getY() > 0 ? boundingBox.getTopFace() : boundingBox.getBottomFace()
        );

        final boolean xCollision = stepAxis(instance, intermediaryPosition, X_AXIS, deltaPosition.getX(),
                intermediaryPosition,
                deltaPosition.getX() < 0 ? boundingBox.getLeftFace() : boundingBox.getRightFace()
        );

        final boolean zCollision = stepAxis(instance, intermediaryPosition, Z_AXIS, deltaPosition.getZ(),
                intermediaryPosition,
                deltaPosition.getZ() > 0 ? boundingBox.getBackFace() : boundingBox.getFrontFace()
        );

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
     * @return true iif a collision has been found
     */
    private static boolean stepAxis(Instance instance, Vector startPosition, Vector axis, float stepAmount, Vector positionOut, Vector... corners) {
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

        float sign = Math.signum(stepAmount);
        final int blockLength = (int) stepAmount;
        final float remainingLength = stepAmount - blockLength;
        // used to determine if 'remainingLength' should be used
        boolean collisionFound = false;
        for (int i = 0; i < Math.abs(blockLength); i++) {
            if (!stepOnce(instance, axis, sign, cornersCopy, cornerPositions)) {
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
            collisionFound |= !stepOnce(instance, direction, remainingLength, cornersCopy, cornerPositions);
        }

        // find the corner which moved the least
        float smallestDisplacement = Float.POSITIVE_INFINITY;
        for (int i = 0; i < corners.length; i++) {
            final float displacement = (float) corners[i].distance(cornersCopy[i]);
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
     * @param cornersCopy     the corners of the bounding box to consider (mutable)
     * @param cornerPositions the corners, converted to BlockPosition (mutable)
     * @return false if this method encountered a collision
     */
    private static boolean stepOnce(Instance instance, Vector axis, float amount, Vector[] cornersCopy, BlockPosition[] cornerPositions) {
        final float sign = Math.signum(amount);
        for (int cornerIndex = 0; cornerIndex < cornersCopy.length; cornerIndex++) {
            Vector corner = cornersCopy[cornerIndex];
            BlockPosition blockPos = cornerPositions[cornerIndex];
            corner.add(axis.getX() * amount, axis.getY() * amount, axis.getZ() * amount);
            blockPos.setX((int) Math.floor(corner.getX()));
            blockPos.setY((int) Math.floor(corner.getY()));
            blockPos.setZ((int) Math.floor(corner.getZ()));

            final Chunk chunk = instance.getChunkAt(blockPos);
            if (!ChunkUtils.isLoaded(chunk)) {
                // Collision at chunk border
                return false;
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

}
