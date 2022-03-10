package net.minestom.server.entity.pathfinding;

import net.minestom.server.entity.pathfinding.task.PathfindTask;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an object which can use the pathfinder.
 * <p>
 * All pathfinder methods are available with {@link #getNavigator()}.
 */
public interface NavigableEntity<T extends Navigator> {
    @NotNull T getNavigator();

    /**
     * Schedules the specified pathfind.
     * @param task The pathfind task.
     */
    default <E extends PathfindTask.Execution<T>> E schedulePathfind(@NotNull PathfindTask<T, E> task) {
        E execution = task.createExecution(getNavigator());
        getNavigator().getPathfindExecutionQueue().addLast(execution);
        return execution;
    }
}
