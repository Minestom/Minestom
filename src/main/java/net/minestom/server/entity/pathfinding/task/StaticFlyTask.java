package net.minestom.server.entity.pathfinding.task;

import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;

public class StaticFlyTask extends StaticMovementTask {

    private final double flySpeed;

    /**
     * Creates a new StaticFlyTask.
     * @param target the target point
     * @param flySpeed the fly speed
     */
    StaticFlyTask(@NotNull Point target, double flySpeed) {
        super(target);
        this.flySpeed = flySpeed;
    }

    @Override
    protected boolean moveTowards(Execution execution, Point point) {
        boolean moved = execution.navigator().moveTowards(point, flySpeed, true);
        return !moved;
    }
}
