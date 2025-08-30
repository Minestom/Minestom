package net.minestom.server.entity.ai.goal;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.pathfinding.Navigator;

import java.time.Duration;

public class FollowTargetGoal extends Goal {
    private final Duration pathDuration;
    private long lastUpdateTime = 0;
    private boolean forceEnd = false;
    private Point lastTargetPos;

    private Entity target;

    /**
     * Creates a follow target goal object.
     *
     * @param entityCreature the entity
     * @param pathDuration   the time between each path update (to check if the target moved)
     */
    public FollowTargetGoal(EntityCreature entityCreature, Duration pathDuration) {
        super(entityCreature);
        this.pathDuration = pathDuration;
    }

    @Override
    public boolean canStart() {
        Entity target = entityCreature.getAi().getTarget();
        if (target == null) return false;
        final boolean result = target.getPosition().distanceSquared(entityCreature.getPosition()) >= 2 * 2;
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
        this.entityCreature.getAi().setTarget(target);
        Navigator navigator = entityCreature.getNavigator();
        this.lastTargetPos = target.getPosition();
        if (lastTargetPos.distanceSquared(entityCreature.getPosition()) < 2 * 2) {
            // Target is too far
            this.forceEnd = true;
            navigator.setPathTo(null);
            return;
        }
        if (navigator.getTargetPosition() == null || !navigator.getTargetPosition().samePoint(lastTargetPos)) {
            navigator.setPathTo(lastTargetPos);
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
        final Pos targetPos = entityCreature.getAi().getTarget() != null ? entityCreature.getAi().getTarget().getPosition() : null;
        if (targetPos != null && !targetPos.sameBlock(lastTargetPos)) {
            this.lastUpdateTime = time;
            this.lastTargetPos = targetPos;
            this.entityCreature.getNavigator().setPathTo(targetPos);
        }
    }

    @Override
    public boolean shouldEnd() {
        final Entity target = entityCreature.getAi().getTarget();
        return forceEnd ||
                target == null ||
                target.isRemoved() ||
                target.getPosition().distanceSquared(entityCreature.getPosition()) < 2 * 2;
    }

    @Override
    public void end() {
        this.entityCreature.getNavigator().setPathTo(null);
    }
}
