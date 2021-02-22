package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.pathfinding.Navigator;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.time.CooldownUtils;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

/**
 * Created by k.shandurenko on 22.02.2021
 */
public class RangedAttackGoal extends GoalSelector {

    private       long     lastShot;
    private final int      delay;
    private final TimeUnit timeUnit;
    private final int      attackRangeSquared;
    private final int      desirableRangeSquared;
    private final boolean  comeClose;

    private boolean stop;

    /**
     * @param entityCreature the entity to add the goal to.
     * @param delay          the delay between each shots.
     * @param attackRange    the allowed range the entity can shoot others.
     * @param desirableRange the desirable range: the entity will try to stay no further than this distance.
     * @param comeClose      whether entity should go as close as possible to the target whether target is not in line of sight.
     * @param timeUnit       the unit of the delay.
     */
    public RangedAttackGoal(@NotNull EntityCreature entityCreature, int delay, int attackRange, int desirableRange, boolean comeClose, @NotNull TimeUnit timeUnit) {
        super(entityCreature);
        this.delay = delay;
        this.timeUnit = timeUnit;
        this.attackRangeSquared = attackRange * attackRange;
        this.desirableRangeSquared = desirableRange * desirableRange;
        this.comeClose = comeClose;
        Check.argCondition(desirableRange <= attackRange, "Desirable range can not exceed attack range!");
    }

    @Override
    public boolean shouldStart() {
        return findAndUpdateTarget() != null;
    }

    @Override
    public void start() {
        Entity target = findAndUpdateTarget();
        Check.notNull(target, "The target is not expected to be null!");
        this.entityCreature.getNavigator().setPathTo(target.getPosition());
    }

    @Override
    public void tick(long time) {
        Entity target = findAndUpdateTarget();
        if (target == null) {
            this.stop = true;
            return;
        }
        double  distanceSquared = this.entityCreature.getDistanceSquared(target);
        boolean comeClose       = false;
        if (distanceSquared <= this.attackRangeSquared) {
            if (!CooldownUtils.hasCooldown(time, this.lastShot, this.timeUnit, this.delay)) {
                if (this.entityCreature.hasLineOfSight(target)) {

                    this.lastShot = time;
                } else {
                    comeClose = this.comeClose;
                }
            }
        }
        Navigator navigator    = this.entityCreature.getNavigator();
        Position  pathPosition = navigator.getPathPosition();
        if (!comeClose && distanceSquared <= this.desirableRangeSquared) {
            if (pathPosition != null) {
                navigator.setPathTo(null);
            }
            return;
        }
        Position targetPosition = target.getPosition();
        if (pathPosition == null || !pathPosition.isSimilar(targetPosition)) {
            navigator.setPathTo(targetPosition);
        }
    }

    @Override
    public boolean shouldEnd() {
        return this.stop;
    }

    @Override
    public void end() {
        // Stop following the target
        this.entityCreature.getNavigator().setPathTo(null);
    }
}
