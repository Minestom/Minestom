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
    private final double maxDistance;

    private long lastUpdateTime = 0;
    private boolean forceEnd = false;

    /**
     * Creates a follow target goal object.
     *
     * @param entityCreature the entity
     * @param pathDuration   the time between each path update (to check if the target moved)
     * @param maxDistance    the desired maximum distance from the target
     */
    public FollowTargetGoal(EntityCreature entityCreature, Duration pathDuration, double maxDistance) {
        super(entityCreature);
        this.pathDuration = pathDuration;
        this.maxDistance = maxDistance;
    }

    @Override
    public boolean canStart() {
        final Entity target = entityCreature.getAi().getTarget();
        if (target == null) return false;
        return entityCreature.getDistanceSquared(target) > maxDistance * maxDistance;
    }

    @Override
    public void start() {
        lastUpdateTime = 0;
        forceEnd = false;

        final Entity target = entityCreature.getAi().getTarget();
        final Navigator navigator = entityCreature.getNavigator();
        assert target != null;

        final Pos targetPos = target.getPosition();
        if (navigator.getTargetPosition() == null || !navigator.getTargetPosition().samePoint(targetPos)) {
            navigator.setPathTo(targetPos);
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

        final Entity target = entityCreature.getAi().getTarget();
        final Navigator navigator = entityCreature.getNavigator();
        assert target != null;

        final Pos targetPos = target.getPosition();
        final Point navigatorPos = navigator.getTargetPosition();
        if (navigatorPos == null || !targetPos.sameBlock(navigatorPos)) {
            this.lastUpdateTime = time;
            navigator.setPathTo(targetPos);
        }
    }

    @Override
    public boolean shouldEnd() {
        final Entity target = entityCreature.getAi().getTarget();
        return forceEnd ||
                target == null ||
                target.isRemoved() ||
                target.getPosition().distanceSquared(entityCreature.getPosition()) < maxDistance * maxDistance;
    }

    @Override
    public void end() {
        entityCreature.getNavigator().setPathTo(null);
    }
}
