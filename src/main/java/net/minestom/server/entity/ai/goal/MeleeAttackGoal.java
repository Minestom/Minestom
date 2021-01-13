package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.time.CooldownUtils;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Attacks the entity's target ({@link EntityCreature#getTarget()}) OR the closest entity
 * which can be targeted with the entity {@link TargetSelector}.
 */
public class MeleeAttackGoal extends GoalSelector {

    private long lastHit;
    private final int delay;
    private final TimeUnit timeUnit;
    private final int range;

    private boolean stop;

    /**
     * @param entityCreature the entity to add the goal to
     * @param delay          the delay between each attacks
     * @param range          the allowed range the entity can attack others.
     * @param timeUnit       the unit of the delay
     */
    public MeleeAttackGoal(@NotNull EntityCreature entityCreature, int delay, int range, @NotNull TimeUnit timeUnit) {
        super(entityCreature);
        this.delay = delay;
        this.range = range;
        this.timeUnit = timeUnit;
    }

    @Override
    public boolean shouldStart() {
        return getTarget() != null;
    }

    @Override
    public void start() {
        final Entity target = getTarget();
        Check.notNull(target, "The target is not expected to be null!");
        final Position targetPosition = target.getPosition();
        entityCreature.setPathTo(targetPosition);
    }

    @Override
    public void tick(long time) {
        final Entity target = getTarget();

        this.stop = target == null;

        if (!stop) {

            // Attack the target entity
            if (entityCreature.getDistance(target) <= range) {
                if (!CooldownUtils.hasCooldown(time, lastHit, timeUnit, delay)) {
                    entityCreature.attack(target, true);
                    this.lastHit = time;
                }
                return;
            }

            // Move toward the target entity
            final Position pathPosition = entityCreature.getPathPosition();
            final Position targetPosition = target.getPosition();
            if (pathPosition == null || !pathPosition.isSimilar(targetPosition)) {
                entityCreature.setPathTo(targetPosition);
            }
        }
    }

    @Override
    public boolean shouldEnd() {
        return stop;
    }

    @Override
    public void end() {
        // Stop following the target
        entityCreature.setPathTo(null);
    }

    /**
     * Use {@link EntityCreature#getTarget()} or
     * the entity target selectors to get the correct target
     *
     * @return the target of the entity
     */
    @Nullable
    private Entity getTarget() {
        final Entity target = entityCreature.getTarget();
        return target == null ? findTarget() : target;
    }
}
