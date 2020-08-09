package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.time.CooldownUtils;
import net.minestom.server.utils.time.TimeUnit;

public class MeleeAttackGoal extends GoalSelector {

    private long lastHit;
    private int delay;
    private TimeUnit timeUnit;

    /**
     * @param entityCreature the entity to add the goal to
     * @param delay          the delay between each attacks
     * @param timeUnit       the unit of the delay
     */
    public MeleeAttackGoal(EntityCreature entityCreature, int delay, TimeUnit timeUnit) {
        super(entityCreature);
        this.delay = delay;
        this.timeUnit = timeUnit;
    }

    @Override
    public boolean shouldStart() {
        return entityCreature.getTarget() != null;
    }

    @Override
    public void start() {
        final Position targetPosition = entityCreature.getTarget().getPosition();
        entityCreature.setPathTo(targetPosition);
    }

    @Override
    public void tick(long time) {
        final Entity target = entityCreature.getTarget();
        if (target != null) {

            if (entityCreature.getBoundingBox().intersect(target)) {
                if (!CooldownUtils.hasCooldown(time, lastHit, timeUnit, delay)) {
                    entityCreature.attack(target, true);
                    this.lastHit = time;
                }
                return;
            }

            final Position pathPosition = entityCreature.getPathPosition();
            final Position targetPosition = target.getPosition();
            if (pathPosition == null || !pathPosition.isSimilar(targetPosition)) {
                entityCreature.setPathTo(targetPosition);
            }
        }
    }

    @Override
    public boolean shouldEnd() {
        return entityCreature.getTarget() == null;
    }

    @Override
    public void end() {

    }
}
