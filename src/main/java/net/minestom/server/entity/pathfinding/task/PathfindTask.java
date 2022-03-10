package net.minestom.server.entity.pathfinding.task;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.pathfinding.NavigableEntity;
import net.minestom.server.entity.pathfinding.engine.PathfindingEngine;
import net.minestom.server.entity.pathfinding.engine.PathfindingResult;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

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
 * @param <E> The execution of the action
 */
public abstract class PathfindTask<T extends NavigableEntity<?>, E extends PathfindTask.Execution<T>> {

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
    public static @NotNull StaticWalkTask walkTo(@NotNull Point target, double walkSpeed) {
        return new StaticWalkTask(target, walkSpeed);
    }

    /**
     * Creates a new pathfinding task used to fly an entity to a fixed position.
     *
     * @param target The position to fly to.
     * @param flySpeed The speed to fly at.
     * @return The new pathfinding task.
     */
    public static @NotNull StaticFlyTask flyTo(@NotNull Point target, double flySpeed) {
        return new StaticFlyTask(target, flySpeed);
    }

    ////////////////////
    // Implementation //
    ////////////////////

    /**
     * Creates the execution object for this task.
     * @param navigator The navigator to use for pathfinding.
     * @return the execution object associated with the completion of the stages of this task.
     */
    @ApiStatus.Internal
    public abstract @NotNull E createExecution(@NotNull T navigator);

    /**
     * This interface represents an immutable view into a pathfind execution.
     * <br><br>
     * Deriving records from this interface is recommended, but assuredly not required.
     */
    public interface Execution<T extends NavigableEntity<?>> {

        /**
         * Returns the task this execution is handling.
         * @return the task this execution is handling.
         */
        @NotNull PathfindTask<?, ?> task();

        /**
         * Returns the navigator that this execution is running for.
         * @return the navigator that this execution is running for.
         */
        @NotNull T navigator();

        /**
         * Returns the pathfinding engine that this execution uses.
         * @return the pathfinding engine that this execution uses.
         */
        @NotNull PathfindingEngine<T> engine();

        /**
         * Returns the hibernation future of this execution.
         * <br><br>
         * The hibernation future will be completed when the execution wakes up from hibernation.
         * An example of when this task wakes up would be when this execution reaches first in an entities pathfinding queue.
         * @return the hibernation future of this execution.
         */
        @NotNull CompletableFuture<Void> hibernation();

        /**
         * Returns the completion future of this execution.
         * <br><br>
         * The completion future will be completed when the execution is completed.
         * An example of this can be found in the {@link StaticMovementTask} class.
         * Within this class, the completion future will be completed when the entity reaches the destination.
         * @return the completion future of this execution.
         */
        @NotNull CompletableFuture<?> completion();

        /**
         * This function is called before the execution is woken from hibernation.
         * If this function returns false, the execution will not be allowed to wake up.
         * Otherwise, it will be allowed to wake up.
         * <br><br>
         * An example usecase is for executions that require entities to be on the ground.
         */
        default boolean allowWakeUp() {
            return true;
        }

        /**
         * This function is called when the execution is cancelled. Users of this method are expected to override
         * and call super before doing anything else.
         * <br>
         * This cancellation should halt all computation and actions associated with this execution.
         */
        default void cancel() {
            hibernation().cancel(true);
            completion().cancel(true);
        }
    }

    /**
     * This interface represents an immutable view into a pathfind execution.
     * <br><br>
     * This interface differs in that it also provides access to a specific pathfind result.
     */
    public interface ExecutionWithResult<T extends NavigableEntity<?>> extends Execution<T> {

        /**
         * Returns the result of the pathfinding.
         * @return the result of the pathfinding.
         */
        @NotNull CompletableFuture<PathfindingResult> result();
    }
}
