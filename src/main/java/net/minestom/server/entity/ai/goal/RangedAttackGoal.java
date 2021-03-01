package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.pathfinding.Navigator;
import net.minestom.server.entity.type.projectile.EntityProjectile;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.time.CooldownUtils;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class RangedAttackGoal extends GoalSelector {

    private long lastShot;
    private final int delay;
    private final TimeUnit timeUnit;
    private final int attackRangeSquared;
    private final int desirableRangeSquared;
    private final boolean comeClose;
    private final double power;
    private final double spread;

    private Function<Entity, EntityProjectile> projectileGenerator;

    private boolean stop;
    private Entity cachedTarget;

    /**
     * @param entityCreature the entity to add the goal to.
     * @param delay          the delay between each shots.
     * @param attackRange    the allowed range the entity can shoot others.
     * @param desirableRange the desirable range: the entity will try to stay no further than this distance.
     * @param comeClose      whether entity should go as close as possible to the target whether target is not in line of sight.
     * @param spread         shot spread (0 for best accuracy).
     * @param power          shot power (1 for normal).
     * @param timeUnit       the unit of the delay.
     */
    public RangedAttackGoal(@NotNull EntityCreature entityCreature, int delay, int attackRange, int desirableRange, boolean comeClose, double power, double spread, @NotNull TimeUnit timeUnit) {
        super(entityCreature);
        this.delay = delay;
        this.timeUnit = timeUnit;
        this.attackRangeSquared = attackRange * attackRange;
        this.desirableRangeSquared = desirableRange * desirableRange;
        this.comeClose = comeClose;
        this.power = power;
        this.spread = spread;
        Check.argCondition(desirableRange > attackRange, "Desirable range can not exceed attack range!");
    }

    public void setProjectileGenerator(Function<Entity, EntityProjectile> projectileGenerator) {
        this.projectileGenerator = projectileGenerator;
    }

    @Override
    public boolean shouldStart() {
        this.cachedTarget = findTarget();
        return this.cachedTarget != null;
    }

    @Override
    public void start() {
        this.entityCreature.getNavigator().setPathTo(this.cachedTarget.getPosition());
    }

    @Override
    public void tick(long time) {
        Entity target;
        if (this.cachedTarget != null) {
            target = this.cachedTarget;
            this.cachedTarget = null;
        } else {
            target = findTarget();
        }
        if (target == null) {
            this.stop = true;
            return;
        }
        double distanceSquared = this.entityCreature.getDistanceSquared(target);
        boolean comeClose = false;
        if (distanceSquared <= this.attackRangeSquared) {
            if (!CooldownUtils.hasCooldown(time, this.lastShot, this.timeUnit, this.delay)) {
                if (this.entityCreature.hasLineOfSight(target)) {
                    Position to = target.getPosition().clone().add(0D, target.getEyeHeight(), 0D);

                    Function<Entity, EntityProjectile> projectileGenerator = this.projectileGenerator;
                    if (projectileGenerator == null) {
                        projectileGenerator = shooter -> new EntityProjectile(shooter, EntityType.ARROW);
                    }
                    EntityProjectile projectile = projectileGenerator.apply(this.entityCreature);
                    projectile.setInstance(this.entityCreature.getInstance(), this.entityCreature.getPosition());

                    projectile.shoot(to, this.power, this.spread);
                    this.lastShot = time;
                } else {
                    comeClose = this.comeClose;
                }
            }
        }
        Navigator navigator = this.entityCreature.getNavigator();
        Position pathPosition = navigator.getPathPosition();
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
