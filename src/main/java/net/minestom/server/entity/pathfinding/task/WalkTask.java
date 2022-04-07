package net.minestom.server.entity.pathfinding.task;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.pathfinding.engine.PathfindingEngine;
import net.minestom.server.utils.PathfindUtils;
import org.jetbrains.annotations.NotNull;

public class WalkTask extends MovementTask<WalkTask.Path> {

    private final double walkSpeed;

    /**
     * Creates a new WalkTask.
     * @param initialTarget the target point
     * @param walkSpeed the walk speed
     */
    WalkTask(@NotNull Point initialTarget, double walkSpeed) {
        super(initialTarget);
        this.walkSpeed = walkSpeed;
    }

    @Override
    public @NotNull Path createPath(@NotNull PathfindingEngine engine, @NotNull Entity entity) {
        return new Path(engine, entity, this);
    }

    public double walkSpeed() {
        return walkSpeed;
    }

    public static class Path extends MovementTask.Path {

        private final WalkTask task;

        protected Path(@NotNull PathfindingEngine engine, @NotNull Entity entity, @NotNull WalkTask task) {
            super(engine, entity, task);
            this.task = task;
        }

        @Override
        protected boolean moveTowards(Point point) {
            boolean moved = PathfindUtils.moveEntity(entity(), point, task.walkSpeed(), false);
            return !moved;
        }
    }
}
