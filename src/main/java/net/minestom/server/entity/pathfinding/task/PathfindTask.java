package net.minestom.server.entity.pathfinding.task;

import net.minestom.server.attribute.Attribute;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.pathfinding.engine.PathfindingEngine;
import net.minestom.server.entity.pathfinding.engine.PathfindingResult;
import net.minestom.server.utils.PathfindUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Queue;

/**
 * This class is used to represent a pathfinding task.
 * <br><br>
 * Some examples include:<br>
 * - Walking to a position<br>
 * - Walking to an entity<br>
 * - Swimming through a river until reaching it's shore<br>
 * - Flying to a position<br>
 * e.t.c.
 *
 * @param <P> The path of the action
 */
public abstract class PathfindTask<P extends PathfindTask.Path> {

    /////////
    // Api //
    /////////

    /**
     * Creates a new pathfinding task used to walk an entity to a fixed position.
     * <br><br>
     * Note that this task will not make the navigator jump.
     * @param target The position to walk to.
     * @param walkSpeed The speed to walk at.
     * @return The new pathfinding task.
     */
    public static @NotNull WalkTask walkTo(@NotNull Point target, double walkSpeed) {
        return new WalkTask(target, walkSpeed);
    }

    /**
     * Creates a new pathfinding task used to walk and jump an entity to a fixed position.
     * @param target The position to walk and jump to.
     * @param walkSpeed The speed to walk at.
     * @param jumpHeight The height to jump at.
     */
    public static @NotNull WalkAndJumpTask walkAndJumpTo(@NotNull Point target, double walkSpeed, double jumpHeight) {
        return new WalkAndJumpTask(target, walkSpeed, jumpHeight);
    }

    /**
     * Creates a new pathfinding task used to fly an entity to a fixed position.
     *
     * @param target The position to fly to.
     * @param flySpeed The speed to fly at.
     * @return The new pathfinding task.
     */
    public static @NotNull FlyTask flyTo(@NotNull Point target, double flySpeed) {
        return new FlyTask(target, flySpeed);
    }

    public static @NotNull MovementTask<?> moveTo(@NotNull Point target) {
        // TODO: Automatically select the best task
        return new WalkAndJumpTask(target, Attribute.MOVEMENT_SPEED.defaultValue(), 1);
    }

    /**
     * Creates a path object for this task, given an entity.
     * @param entity The entity to pathfind.
     * @return the path object associated with the entity's path.
     */
    public P start(@NotNull EntityCreature entity) {
        return createPath(PathfindUtils.getEngine(entity), entity);
    }

    ////////////////////
    // Implementation //
    ////////////////////

    /**
     * Creates a path object for this task.
     * @param engine The pathfinding engine.
     * @param entity The entity to pathfind.
     * @return the path object associated with the completion of the stages of this task.
     */
    @ApiStatus.Internal
    protected abstract @NotNull P createPath(@NotNull PathfindingEngine engine, @NotNull Entity entity);

    /**
     * This class represents an immutable view into a running, or already completed pathfind.
     * <br><br>
     * Derivatives of this class may give the ability to change various aspects of the path.
     */
    public abstract static class Path {

        private final @NotNull PathfindingEngine engine;
        private final @NotNull Entity entity;

        protected Path(@NotNull PathfindingEngine engine, @NotNull Entity entity) {
            this.engine = engine;
            this.entity = entity;
        }

        /**
         * Returns the next point to walk to from the current entity position.
         * @return The next point to walk to, null if the path is finished, stuck, or not calculated yet.
         */
        public abstract @Nullable Point nextPoint();

        /**
         * Updates the path's target position to the specified position.
         * @param target The new target position, null to cancel the path.
         */
        public abstract void updateTarget(@Nullable Point target);

        /**
         * Calculates the full path to the target point.
         * <br><br>
         * This is a blocking operation, generally only used for testing.
         * @return The full path to the target point, null if the path cannot be calculated.
         */
        public abstract @Nullable Queue<Point> fullPath();

        /**
         * Returns the pathfinding engine associated with this path.
         * @return The pathfinding engine associated with this path.
         */
        public @NotNull PathfindingEngine engine() {
            return engine;
        }

        /**
         * Returns the entity associated with this path.
         * @return The entity associated with this path.
         */
        public @NotNull Entity entity() {
            return entity;
        }
    }
}
