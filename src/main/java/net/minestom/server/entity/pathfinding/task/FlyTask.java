package net.minestom.server.entity.pathfinding.task;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.pathfinding.engine.PathfindingEngine;
import net.minestom.server.utils.PathfindUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Queue;

public class FlyTask extends MovementTask<FlyTask.Path> {

    private final double flySpeed;

    /**
     * Creates a new StaticFlyTask.
     * @param initialTarget the target point
     * @param flySpeed the fly speed
     */
    FlyTask(@NotNull Point initialTarget, double flySpeed) {
        super(initialTarget);
        this.flySpeed = flySpeed;
    }

    @Override
    public @NotNull Path createPath(@NotNull PathfindingEngine engine, @NotNull Entity entity) {
        return new Path(engine, entity, this);
    }

    public double flySpeed() {
        return flySpeed;
    }

    public static class Path extends MovementTask.Path {

        private final FlyTask task;

        protected Path(@NotNull PathfindingEngine engine, @NotNull Entity entity, @NotNull FlyTask task) {
            super(engine, entity, task);
            this.task = task;
        }

        @Override
        protected boolean moveTowards(Point point) {
            boolean moved = PathfindUtils.moveEntity(entity(), point, task.flySpeed(), true);
            return !moved;
        }
    }
}
