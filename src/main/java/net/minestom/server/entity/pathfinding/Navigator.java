package net.minestom.server.entity.pathfinding;

import com.extollit.gaming.ai.path.HydrazinePathFinder;
import com.extollit.gaming.ai.path.PathOptions;
import com.extollit.gaming.ai.path.model.IPath;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.WorldBorder;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.position.PositionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// TODO all pathfinding requests could be processed in another thread

/**
 * Necessary object for all {@link NavigableEntity}.
 */
public class Navigator {

    private final PFPathingEntity pathingEntity;
    private HydrazinePathFinder pathFinder;
    private IPath path;
    private Point pathPosition;

    private final Entity entity;

    public Navigator(@NotNull Entity entity) {
        this.entity = entity;
        this.pathingEntity = new PFPathingEntity(this);
    }

    /**
     * Used to move the entity toward {@code direction} in the X and Z axis
     * Gravity is still applied but the entity will not attempt to jump
     * Also update the yaw/pitch of the entity to look along 'direction'
     *
     * @param direction the targeted position
     * @param speed     define how far the entity will move
     */
    public void moveTowards(@NotNull Point direction, double speed) {
        final Pos position = entity.getPosition();
        final double dx = direction.x() - position.x();
        final double dy = direction.y() - position.y();
        final double dz = direction.z() - position.z();
        // the purpose of these few lines is to slow down entities when they reach their destination
        final double distSquared = dx * dx + dy * dy + dz * dz;
        if (speed > distSquared) {
            speed = distSquared;
        }
        final double radians = Math.atan2(dz, dx);
        final double speedX = Math.cos(radians) * speed;
        final double speedY = dy * speed;
        final double speedZ = Math.sin(radians) * speed;
        final float yaw = PositionUtils.getLookYaw(dx, dz);
        final float pitch = PositionUtils.getLookPitch(dx, direction.y(), dz);
        // Prevent ghosting
        final var physicsResult = CollisionUtils.handlePhysics(entity, new Vec(speedX, speedY, speedZ));
        this.entity.refreshPosition(physicsResult.newPosition().withView(yaw, pitch));
    }

    public void jump(float height) {
        // FIXME magic value
        this.entity.setVelocity(new Vec(0, height * 2.5f, 0));
    }

    /**
     * Retrieves the path to {@code position} and ask the entity to follow the path.
     * <p>
     * Can be set to null to reset the pathfinder.
     * <p>
     * The position is cloned, if you want the entity to continually follow this position object
     * you need to call this when you want the path to update.
     *
     * @param point      the position to find the path to, null to reset the pathfinder
     * @param bestEffort whether to use the best-effort algorithm to the destination,
     *                   if false then this method is more likely to return immediately
     * @return true if a path has been found
     */
    public synchronized boolean setPathTo(@Nullable Point point, boolean bestEffort) {
        if (point != null && pathPosition != null && point.samePoint(pathPosition)) {
            // Tried to set path to the same target position
            return false;
        }

        final Instance instance = entity.getInstance();

        if (pathFinder == null) {
            // Unexpected error
            return false;
        }

        pathFinder.reset();
        if (point == null) {
            return false;
        }

        // Can't path with a null instance.
        if (instance == null) {
            return false;
        }

        // Can't path outside the world border
        final WorldBorder worldBorder = instance.getWorldBorder();
        if (!worldBorder.isInside(point)) {
            return false;
        }

        // Can't path in an unloaded chunk
        final Chunk chunk = instance.getChunkAt(point);
        if (!ChunkUtils.isLoaded(chunk)) {
            return false;
        }

        final PathOptions pathOptions = new PathOptions()
                .targetingStrategy(bestEffort ? PathOptions.TargetingStrategy.gravitySnap :
                        PathOptions.TargetingStrategy.none);
        final IPath path = pathFinder.initiatePathTo(
                point.x(),
                point.y(),
                point.z(),
                pathOptions);
        this.path = path;

        final boolean success = path != null;
        this.pathPosition = success ? point : null;

        return success;
    }

    /**
     * @see #setPathTo(Point, boolean) with {@code bestEffort} sets to {@code true}.
     */
    public boolean setPathTo(@Nullable Point position) {
        return setPathTo(position, true);
    }

    public synchronized void tick(float speed) {
        // No pathfinding tick for dead entities
        if (entity instanceof LivingEntity && ((LivingEntity) entity).isDead())
            return;

        if (pathPosition != null) {
            IPath path = pathFinder.updatePathFor(pathingEntity);
            this.path = path;

            if (path != null) {
                final Point targetPosition = pathingEntity.getTargetPosition();
                if (targetPosition != null) {
                    moveTowards(targetPosition, speed);
                }
            } else {
                if (pathPosition != null) {
                    this.pathPosition = null;
                    pathFinder.reset();
                }
            }
        }
    }

    /**
     * Gets the pathing entity.
     * <p>
     * Used by the pathfinder.
     *
     * @return the pathing entity
     */
    @NotNull
    public PFPathingEntity getPathingEntity() {
        return pathingEntity;
    }

    /**
     * Gets the assigned pathfinder.
     * <p>
     * Can be null if the navigable element hasn't been assigned to an {@link Instance} yet.
     *
     * @return the current pathfinder, null if none
     */
    @Nullable
    public HydrazinePathFinder getPathFinder() {
        return pathFinder;
    }

    public void setPathFinder(@Nullable HydrazinePathFinder pathFinder) {
        this.pathFinder = pathFinder;
    }

    /**
     * Gets the target pathfinder position.
     *
     * @return the target pathfinder position, null if there is no one
     */
    public @Nullable Point getPathPosition() {
        return pathPosition;
    }

    /**
     * Changes the position this element is trying to reach.
     *
     * @param pathPosition the new current path position
     * @deprecated Please use {@link #setPathTo(Point)}
     */
    @Deprecated
    public void setPathPosition(@Nullable Point pathPosition) {
        this.pathPosition = pathPosition;
    }

    @Nullable
    public IPath getPath() {
        return path;
    }

    public void setPath(@Nullable IPath path) {
        this.path = path;
    }

    @NotNull
    public Entity getEntity() {
        return entity;
    }
}
