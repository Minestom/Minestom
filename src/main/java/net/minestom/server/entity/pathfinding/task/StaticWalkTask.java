package net.minestom.server.entity.pathfinding.task;

import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;

public class StaticWalkTask extends StaticMovementTask {

    private final double walkSpeed;

    /**
     * Creates a new StaticWalkTask.
     * @param target the target point
     * @param walkSpeed the walk speed
     */
    StaticWalkTask(@NotNull Point target, double walkSpeed) {
        super(target);
        this.walkSpeed = walkSpeed;
    }

    @Override
    protected boolean moveTowards(Execution execution, Point point) {
        boolean moved = execution.navigator().moveTowards(point, walkSpeed, false);
        return !moved;
    }
}
