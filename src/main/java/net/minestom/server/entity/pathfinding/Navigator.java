package net.minestom.server.entity.pathfinding;

import net.minestom.server.attribute.Attribute;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.pathfinding.engine.astar.AStarEngine;
import net.minestom.server.entity.pathfinding.task.PathfindTask;
import net.minestom.server.entity.pathfinding.engine.PathfindingEngine;
import net.minestom.server.entity.pathfinding.task.StaticPathfindTask;
import net.minestom.server.entity.pathfinding.task.StaticMovementTask;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.position.PositionUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

// TODO all pathfinding requests could be processed in another thread

/**
 * Necessary object for all {@link NavigableEntity}.
 */
public final class Navigator implements NavigableEntity<Navigator> {

    // Pathfinding tasks
    private final Deque<PathfindTask.Execution<?>> pathfindExecutions = new ArrayDeque<>();
    private PathfindTask.Execution<?> currentPathfindExecution;
    private PathfindingEngine<? extends Navigator> engine;

    private final Entity entity;

    public Navigator(@NotNull Entity entity) {
        this.entity = entity;
    }

    @ApiStatus.Internal
    public synchronized void tick(long time) {
        // Tick pathfinding
        tickTraversal(time);
    }

    /**
     * Returns the entity's mutable pathfinding queue.
     * @return The entity's mutable pathfinding queue.
     */
    public @NotNull Deque<PathfindTask.Execution<?>> getPathfindExecutionQueue() {
        return pathfindExecutions;
    }

    /**
     * Returns the currently running pathfinding execution if one is running, false otherwise.
     * @return The currently running pathfinding execution if one is running, false otherwise.
     */
    public @Nullable PathfindTask.Execution<?> getCurrentPathfindExecution() {
        return currentPathfindExecution;
    }

    /**
     * Gets the current position of this path traverser
     * @return the current position of this path traverser
     */
    public @NotNull Pos getPosition() {
        return entity.getPosition();
    }

    /**
     * Gets the bounding box of this path traverser
     * @return the bounding box of this path traverser
     */
    public @NotNull BoundingBox getBoundingBox() {
        return entity.getBoundingBox();
    }

    /**
     * Gets the pathfinding engine of this path traverser
     * @return the pathfinding engine of this path traverser
     */
    @SuppressWarnings("unchecked")
    public @NotNull <T extends Navigator> PathfindingEngine<T> getPathfindingEngine(@NotNull PathfindTask<T, ?> task) {
        if (engine == null) {
            // TODO: make this configurable
            engine = new AStarEngine(Objects.requireNonNull(getEntity().getInstance()), 0.05);
        }
        return (PathfindingEngine<T>) engine;
    }

    private void tickTraversal(long time) {
        // Return if there is a current pathfinding task.
        if (currentPathfindExecution != null) {
            return;
        }

        Deque<PathfindTask.Execution<?>> pathfindTasks = getPathfindExecutionQueue();
        PathfindTask.Execution<?> execution = pathfindTasks.peek();

        if (execution == null) {
            return;
        }

        if (execution.allowWakeUp()) {
            pathfindTasks.poll();
            execution.hibernation().complete(null);
            currentPathfindExecution = execution;
            execution.completion().thenRun(() -> currentPathfindExecution = null);
        }
    }

    private void reset() {
        // TODO: Add a way for pathfind executions to reset
        pathfindExecutions.clear();
        if (currentPathfindExecution != null) {
            currentPathfindExecution.cancel();
        }
    }

    /**
     * Used to move the entity toward {@code direction} in the X and Z axis
     * Gravity is still applied, so the entity will fall if it's not on a solid block.
     * Also update the yaw/pitch of the entity to look along 'direction'
     *
     * @param direction the targeted position
     * @param speed     define how far the entity will move
     * @return true if the entity moved, false otherwise
     */
    public boolean moveTowards(@NotNull Point direction, double speed, boolean moveUpwards) {
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
        final double speedY = moveUpwards ? dy * speed : 0;
        final double speedZ = Math.sin(radians) * speed;
        final float yaw = PositionUtils.getLookYaw(dx, dz);
        final float pitch = PositionUtils.getLookPitch(dx, dy, dz);
        // Prevent ghosting
        final var physicsResult = CollisionUtils.handlePhysics(entity, new Vec(speedX, speedY, speedZ));
        if (position.equals(physicsResult.newPosition())) {
            return false;
        }
        this.entity.refreshPosition(physicsResult.newPosition().withView(yaw, pitch));
        return true;
    }

    public void jump(float height) {
        // FIXME magic value
        this.entity.setVelocity(new Vec(0, height * 2.5f, 0));
    }

    // Recursive navigable

    @Override
    public @NotNull Navigator getNavigator() {
        return this;
    }

    // Previous deprecated api support

    public @NotNull Entity getEntity() {
        return entity;
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
    @Deprecated
    public synchronized CompletableFuture<Boolean> setPathTo(@Nullable Point point, boolean bestEffort) {
        reset();

        if (point == null) {
            return CompletableFuture.completedFuture(false);
        }

        float speed;

        if (entity instanceof EntityCreature creature) {
            speed = creature.getAttributeValue(Attribute.MOVEMENT_SPEED);
        } else {
            speed = Attribute.MOVEMENT_SPEED.defaultValue();
        }

        StaticMovementTask walkTask = PathfindTask.walkTo(point, speed);
        StaticMovementTask.Execution execution = schedulePathfind(walkTask);

        CompletableFuture<Boolean> future = new CompletableFuture<>();

        // Once the result has been made
        execution.result().thenAccept(result -> {
            // Once the full path has been found, or has failed
            result.fullPath().thenAccept(path -> {
                // return true if the path has been found, false otherwise
                future.complete(path != null);
            });
        });

        return future;
    }

    /**
     * @see #setPathTo(Point, boolean) with {@code bestEffort} sets to {@code true}.
     * @return true if a path has been found
     */
    @Deprecated(forRemoval = true)
    public CompletableFuture<Boolean> setPathTo(@Nullable Point position) {
        return setPathTo(position, true);
    }


    /**
     * Gets the target pathfinder position.
     *
     * @return the target pathfinder position, null if there is no one
     */
    @Deprecated(forRemoval = true)
    public @Nullable Point getPathPosition() {
        if (currentPathfindExecution.task() instanceof StaticPathfindTask task) {
            return task.getTarget();
        }
        return null;
    }

    public Block.Getter getBlockGetter() {
        return entity.getInstance();
    }

    public void nextTick(Runnable runnable) {
        entity.scheduleNextTick(ignored -> runnable.run());
    }

    public boolean isTouching(Point target, double delta) {
        BoundingBox box = getBoundingBox();
        Pos pos = entity.getPosition();

        Point start = pos.add(box.relativeStart());
        Point end = pos.add(box.relativeEnd());
        return !(target.x() < start.x() - delta ||
                target.x() > end.x() + delta ||
                target.y() < start.y() - delta ||
                target.y() > end.y() + delta ||
                target.z() < start.z() - delta ||
                target.z() > end.z() + delta);
    }
}
