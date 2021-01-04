package net.minestom.server.entity.pathfinding;

import com.extollit.gaming.ai.path.HydrazinePathFinder;
import com.extollit.gaming.ai.path.SchedulingPriority;
import com.extollit.gaming.ai.path.model.IPath;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.WorldBorder;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.Vector;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.position.PositionUtils;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an entity which can use the pathfinder.
 */
public interface NavigableEntity {

    /**
     * Used to move the entity toward {@code direction} in the X and Z axis
     * Gravity is still applied but the entity will not attempt to jump
     * Also update the yaw/pitch of the entity to look along 'direction'
     *
     * @param direction the targeted position
     * @param speed     define how far the entity will move
     */
    default void moveTowards(@NotNull Position direction, float speed) {
        Check.notNull(direction, "The direction cannot be null");

        final Position position = getNavigableEntity().getPosition();

        final float currentX = position.getX();
        final float currentY = position.getY();
        final float currentZ = position.getZ();

        final float targetX = direction.getX();
        final float targetY = direction.getY();
        final float targetZ = direction.getZ();

        final float dx = targetX - currentX;
        final float dy = targetY - currentY;
        final float dz = targetZ - currentZ;

        // the purpose of these few lines is to slow down entities when they reach their destination
        final float distSquared = dx * dx + dy * dy + dz * dz;
        if (speed > distSquared) {
            speed = distSquared;
        }

        final float radians = (float) Math.atan2(dz, dx);
        final float speedX = (float) (Math.cos(radians) * speed);
        final float speedY = dy * speed;
        final float speedZ = (float) (Math.sin(radians) * speed);

        // Update 'position' view
        PositionUtils.lookAlong(position, dx, direction.getY(), dz);

        Position newPosition = new Position();
        Vector newVelocityOut = new Vector();

        // Prevent ghosting
        CollisionUtils.handlePhysics(getNavigableEntity(),
                new Vector(speedX, speedY, speedZ),
                newPosition, newVelocityOut);

        // Will move the entity during Entity#tick
        position.copyCoordinates(newPosition);
    }

    default void jump(float height) {
        // FIXME magic value
        final Vector velocity = new Vector(0, height * 2.5f, 0);
        getNavigableEntity().setVelocity(velocity);
    }

    /**
     * Retrieves the path to {@code position} and ask the entity to follow the path.
     * <p>
     * Can be set to null to reset the pathfinder.
     * <p>
     * The position is cloned, if you want the entity to continually follow this position object
     * you need to call this when you want the path to update.
     *
     * @param position   the position to find the path to, null to reset the pathfinder
     * @param bestEffort whether to use the best-effort algorithm to the destination,
     *                   if false then this method is more likely to return immediately
     * @return true if a path has been found
     */
    default boolean setPathTo(@Nullable Position position, boolean bestEffort) {
        if (position != null && getPathPosition() != null && position.isSimilar(getPathPosition())) {
            // Tried to set path to the same target position
            return false;
        }

        final Instance instance = getNavigableEntity().getInstance();
        final HydrazinePathFinder pathFinder = getPathFinder();

        if (pathFinder == null) {
            // Unexpected error
            return false;
        }

        pathFinder.reset();
        if (position == null) {
            return false;
        }

        // Can't path outside of the world border
        final WorldBorder worldBorder = instance.getWorldBorder();
        if (!worldBorder.isInside(position)) {
            return false;
        }

        // Can't path in an unloaded chunk
        final Chunk chunk = instance.getChunkAt(position);
        if (!ChunkUtils.isLoaded(chunk)) {
            return false;
        }

        final Position targetPosition = position.clone();

        final IPath path = pathFinder.initiatePathTo(
                targetPosition.getX(),
                targetPosition.getY(),
                targetPosition.getZ(),
                bestEffort);
        setPath(path);

        final boolean success = path != null;
        setPathPosition(success ? targetPosition : null);

        return success;
    }

    /**
     * @see #setPathTo(Position, boolean) with {@code bestEffort} sets to {@code true}.
     */
    default boolean setPathTo(@Nullable Position position) {
        return setPathTo(position, true);
    }

    default void pathFindingTick(float speed) {
        final Position pathPosition = getPathPosition();
        if (pathPosition != null) {
            final HydrazinePathFinder pathFinder = getPathFinder();

            IPath path = pathFinder.updatePathFor(getPathingEntity());
            setPath(path);

            if (path != null) {
                final Position targetPosition = getPathingEntity().getTargetPosition();
                if (targetPosition != null) {
                    moveTowards(targetPosition, speed);
                }
            } else {
                if (pathPosition != null) {
                    setPathPosition(null);
                    pathFinder.reset();
                }
            }
        }
    }

    /**
     * Changes the pathfinding priority for this entity.
     *
     * @param schedulingPriority the new scheduling priority
     * @see <a href="https://github.com/MadMartian/hydrazine-path-finding#path-finding-scheduling">Scheduling Priority</a>
     */
    default void setPathfindingPriority(@NotNull SchedulingPriority schedulingPriority) {
        final HydrazinePathFinder pathFinder = getPathFinder();
        if (pathFinder != null) {
            pathFinder.schedulingPriority(schedulingPriority);
        }
    }

    /**
     * Gets the target pathfinder position.
     *
     * @return the target pathfinder position, null if there is no one
     */
    @Nullable
    Position getPathPosition();

    /**
     * Changes the position this element is trying to reach.
     *
     * @param path the new current path position
     * @deprecated Please use {@link #setPathTo(Position)}
     */
    @Deprecated
    void setPathPosition(@Nullable Position path);

    @Nullable
    IPath getPath();

    void setPath(@Nullable IPath path);

    /**
     * Gets the pathing entity.
     * <p>
     * Used by the pathfinder.
     *
     * @return the pathing entity
     */
    @NotNull
    PFPathingEntity getPathingEntity();

    /**
     * Gets the assigned pathfinder.
     * <p>
     * Can be null if the navigable element hasn't been assigned to an {@link Instance} yet.
     *
     * @return the current pathfinder, null if none
     */
    @Nullable
    HydrazinePathFinder getPathFinder();

    /**
     * Gets the entity concerned by this navigable implementation.
     *
     * @return the navigable entity
     */
    @NotNull
    Entity getNavigableEntity();

}
