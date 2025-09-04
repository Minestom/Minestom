package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.EntityAI;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.utils.time.Cooldown;

import java.time.Duration;
import java.time.temporal.TemporalUnit;

/**
 * Attacks the entity's target ({@link EntityAI#getTarget()}) OR the closest entity
 * which can be targeted with the entity {@link TargetSelector}.
 */
public class MeleeAttackGoal extends Goal {
    private long lastHit;
    private final double range;
    private final Duration delay;

    /**
     * @param entityCreature the entity to add the goal to
     * @param range          the allowed range the entity can attack others.
     * @param delay          the delay between each attacks
     * @param timeUnit       the unit of the delay
     */
    public MeleeAttackGoal(EntityCreature entityCreature, double range, int delay, TemporalUnit timeUnit) {
        this(entityCreature, range, Duration.of(delay, timeUnit));
    }

    /**
     * @param entityCreature the entity to add the goal to
     * @param range          the allowed range the entity can attack others.
     * @param delay          the delay between each attacks
     */
    public MeleeAttackGoal(EntityCreature entityCreature, double range, Duration delay) {
        super(entityCreature);
        this.range = range;
        this.delay = delay;
    }

    @Override
    public boolean canStart() {
        final Entity target = entityCreature.getAi().getTarget();
        return target != null && entityCreature.getDistanceSquared(target) <= range * range;
    }

    @Override
    public void start() {

    }

    @Override
    public void tick(long time) {
        final Entity target = entityCreature.getAi().getTarget();
        assert target != null;

        // Attack the target entity
        if (entityCreature.getDistanceSquared(target) <= range * range) {
            entityCreature.lookAt(target);
            if (!Cooldown.hasCooldown(time, lastHit, delay)) {
                entityCreature.attack(target, true);
                this.lastHit = time;
            }
        }
    }

    @Override
    public boolean shouldEnd() {
        final Entity target = entityCreature.getAi().getTarget();
        return target == null || entityCreature.getDistanceSquared(target) > range * range;
    }

    @Override
    public void end() {

    }
}
