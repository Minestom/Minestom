package net.minestom.server.entity.ai.goal;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.pathfinding.task.PathfindTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

public class FollowTargetGoal extends GoalSelector {
    private final Duration pathDuration;
    private long lastUpdateTime = 0;
    private boolean forceEnd = false;
    private Point lastTargetPos;

    private Entity target;

    // Pathfinding path
    private @Nullable PathfindTask.Path path;

    /**
     * Creates a follow target goal object.
     *
     * @param entityCreature the entity
     * @param pathDuration   the time between each path update (to check if the target moved)
     */
    public FollowTargetGoal(@NotNull EntityCreature entityCreature, @NotNull Duration pathDuration) {
        super(entityCreature);
        this.pathDuration = pathDuration;
    }

    @Override
    public boolean shouldStart() {
        Entity target = entityCreature.getTarget();
        if (target == null) target = findTarget();
        if (target == null) return false;
        final boolean result = target.getPosition().distance(entityCreature.getPosition()) >= 2;
        if (result) {
            this.target = target;
        }
        return result;
    }

    @Override
    public void start() {
        lastUpdateTime = 0;
        forceEnd = false;
        lastTargetPos = null;
        if (target == null) {
            // No defined target
            this.forceEnd = true;
            return;
        }
        this.entityCreature.setTarget(target);
        this.lastTargetPos = target.getPosition();
        if (lastTargetPos.distance(entityCreature.getPosition()) < 2) {
            // Target is too far
            this.forceEnd = true;
            return;
        }
        if (path == null) {
            path = PathfindTask.moveTo(lastTargetPos).start(entityCreature);
        } else {
            forceEnd = true;
        }
    }

    @Override
    public void tick(long time) {
        if (forceEnd ||
                pathDuration.isZero() ||
                pathDuration.toMillis() + lastUpdateTime > time) {
            return;
        }
        final Pos targetPos = entityCreature.getTarget() != null ? entityCreature.getTarget().getPosition() : null;
        if (targetPos != null && !targetPos.samePoint(lastTargetPos)) {
            this.lastUpdateTime = time;
            this.lastTargetPos = targetPos;
            if (path != null) {
                path.updateTarget(targetPos);
            }
        }
    }

    @Override
    public boolean shouldEnd() {
        final Entity target = entityCreature.getTarget();
        return forceEnd ||
                target == null ||
                target.isRemoved() ||
                target.getPosition().distance(entityCreature.getPosition()) < 2;
    }

    @Override
    public void end() {
        if (path != null) {
            path.updateTarget(null);
        }
    }
}
