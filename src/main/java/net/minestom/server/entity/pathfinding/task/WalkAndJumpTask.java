package net.minestom.server.entity.pathfinding.task;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.pathfinding.engine.PathfindingEngine;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.PathfindUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class WalkAndJumpTask extends WalkTask {
    private final double jumpHeight;

    /**
     * Creates a new StaticWalkTask.
     *
     * @param target     the target point
     * @param walkSpeed  the walk speed
     * @param jumpHeight the jump height
     */
    WalkAndJumpTask(@NotNull Point target, double walkSpeed, double jumpHeight) {
        super(target, walkSpeed);
        this.jumpHeight = jumpHeight;
    }

    public double jumpHeight() {
        return jumpHeight;
    }

    public static class Path extends WalkTask.Path {

        private final WalkAndJumpTask task;

        protected Path(@NotNull PathfindingEngine engine, @NotNull Entity entity, @NotNull WalkAndJumpTask task) {
            super(engine, entity, task);
            this.task = task;
        }

        @Override
        protected boolean moveTowards(Point target) {
            boolean isStuck = super.moveTowards(target);
            var entity = entity();
            Point pos = entity.getPosition();

            // Ensure that the next target is above before jumping
            double verticalDistance = target.y() - pos.y();
            if (verticalDistance > 0.4) {
                // Ensure that the entity is on the ground before jumping
                pos = pos.sub(0, 0.01, 0);
                Instance instance = Objects.requireNonNull(entity.getInstance(),
                        "Instance must not be null while pathfinding.");
                if (instance.getBlock(pos).isSolid()) {
                    PathfindUtils.jump(entity, task.jumpHeight());
                }
            }
            return isStuck;
        }
    }
}