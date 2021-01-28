package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.pathfinding.Navigator;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.time.UpdateOption;
import org.jetbrains.annotations.NotNull;

public class FollowTargetGoal extends GoalSelector {

    private final UpdateOption pathUpdateOption;
    private long lastUpdateTime = 0;
    private boolean forceEnd = false;
    private Position lastTargetPos;

    /**
     * Creates a follow target goal object.
     *
     * @param entityCreature   the entity
     * @param pathUpdateOption the time between each path update (to check if the target moved)
     */
    public FollowTargetGoal(@NotNull EntityCreature entityCreature, @NotNull UpdateOption pathUpdateOption) {
        super(entityCreature);
        this.pathUpdateOption = pathUpdateOption;
    }

    @Override
    public boolean shouldStart() {
        return entityCreature.getTarget() != null &&
                getDistance(entityCreature.getTarget().getPosition(), entityCreature.getPosition()) >= 2;
    }

    @Override
    public void start() {
        lastUpdateTime = 0;
        forceEnd = false;
        lastTargetPos = null;
        final Entity target = entityCreature.getTarget();

        if (target != null) {
            Navigator navigator = entityCreature.getNavigator();

            lastTargetPos = target.getPosition().clone();
            if (getDistance(lastTargetPos, entityCreature.getPosition()) < 2) {
                forceEnd = true;
                navigator.setPathTo(null);
                return;
            }

            if (navigator.getPathPosition() == null ||
                    (!navigator.getPathPosition().isSimilar(lastTargetPos))) {
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
                pathUpdateOption.getValue() == 0 ||
                pathUpdateOption.getTimeUnit().toMilliseconds(pathUpdateOption.getValue()) + lastUpdateTime > time) {
            return;
        }
        Position targetPos = entityCreature.getTarget() != null ? entityCreature.getTarget().getPosition() : null;
        if (targetPos != null && !targetPos.equals(lastTargetPos)) {
            lastUpdateTime = time;
            lastTargetPos.copy(lastTargetPos);
            entityCreature.getNavigator().setPathTo(targetPos);
        }
    }

    @Override
    public boolean shouldEnd() {
        return forceEnd ||
                entityCreature.getTarget() == null ||
                getDistance(entityCreature.getTarget().getPosition(), entityCreature.getPosition()) < 2;
    }

    @Override
    public void end() {
        entityCreature.getNavigator().setPathTo(null);
    }

    private double getDistance(@NotNull Position a, @NotNull Position b) {
        return MathUtils.square(a.getX() - b.getX()) +
                MathUtils.square(a.getZ() - b.getZ());
    }
}
