package net.minestom.server.entity.ai.goal;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.pathfinding.Navigator;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class FollowTargetGoal extends GoalSelector {
    private final Duration pathDuration;
    private long lastUpdateTime = 0;
    private boolean forceEnd = false;
    private Point lastTargetPos;

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
        return entityCreature.getTarget() != null &&
                entityCreature.getTarget().getPosition().distance(entityCreature.getPosition()) >= 2;
    }

    @Override
    public void start() {
        lastUpdateTime = 0;
        forceEnd = false;
        lastTargetPos = null;
        final Entity target = entityCreature.getTarget();
        if (target != null) {
            Navigator navigator = entityCreature.getNavigator();

            lastTargetPos = target.getPosition();
            if (lastTargetPos.distance(entityCreature.getPosition()) < 2) {
                forceEnd = true;
                navigator.setPathTo(null);
                return;
            }

            if (navigator.getPathPosition() == null ||
                    (!navigator.getPathPosition().samePoint(lastTargetPos))) {
                navigator.setPathTo(lastTargetPos);
            } else {
                forceEnd = true;
            }
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
        final var targetPos = entityCreature.getTarget() != null ? entityCreature.getTarget().getPosition() : null;
        if (targetPos != null && !targetPos.samePoint(lastTargetPos)) {
            this.lastUpdateTime = time;
            this.lastTargetPos = targetPos;
            this.entityCreature.getNavigator().setPathTo(targetPos);
        }
    }

    @Override
    public boolean shouldEnd() {
        final Entity target = entityCreature.getTarget();
        return forceEnd ||
                target == null ||
                target.getPosition().distance(entityCreature.getPosition()) < 2;
    }

    @Override
    public void end() {
        entityCreature.getNavigator().setPathTo(null);
    }
}
