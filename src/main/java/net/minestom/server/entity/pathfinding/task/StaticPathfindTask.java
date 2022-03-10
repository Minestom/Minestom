package net.minestom.server.entity.pathfinding.task;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.pathfinding.NavigableEntity;
import net.minestom.server.entity.pathfinding.engine.PathfindingResult;
import org.jetbrains.annotations.NotNull;

public abstract class StaticPathfindTask<
        T extends NavigableEntity<?>,
        E extends PathfindTask.Execution<T>
    > extends PathfindTask<T, E> {

    private final @NotNull Point target;

    public StaticPathfindTask(@NotNull Point target) {
        this.target = target;
    }

    public @NotNull Point getTarget() {
        return target;
    }

    protected @NotNull PathfindingResult pathfind(@NotNull E execution) {
        return execution.engine().findN2P(execution.navigator(), target);
    }
}
