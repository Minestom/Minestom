package net.minestom.server.entity.ai.goal;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.pathfinding.Navigator;
import net.minestom.server.utils.time.Cooldown;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.function.Function;

public class RangedAttackGoal extends GoalSelector {
    private final Cooldown cooldown = new Cooldown(Duration.of(5, TimeUnit.SERVER_TICK));

    private long lastShot;
    private final Duration delay;
    private final int attackRangeSquared;
    private final int desirableRangeSquared;
    private final boolean comeClose;
    private final double power;
    private final double spread;

    private ProjectileGenerator projectileGenerator;

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
    public RangedAttackGoal(@NotNull EntityCreature entityCreature, int delay, int attackRange, int desirableRange, boolean comeClose, double power, double spread, @NotNull TemporalUnit timeUnit) {
        this(entityCreature, Duration.of(delay, timeUnit), attackRange, desirableRange, comeClose, power, spread);
    }

    /**
     * @param entityCreature the entity to add the goal to.
     * @param delay          the delay between each shots.
     * @param attackRange    the allowed range the entity can shoot others.
     * @param desirableRange the desirable range: the entity will try to stay no further than this distance.
     * @param comeClose      whether entity should go as close as possible to the target whether target is not in line of sight.
     * @param spread         shot spread (0 for best accuracy).
     * @param power          shot power (1 for normal).
     */
    public RangedAttackGoal(@NotNull EntityCreature entityCreature, Duration delay, int attackRange, int desirableRange, boolean comeClose, double power, double spread) {
        super(entityCreature);
        this.delay = delay;
        this.attackRangeSquared = attackRange * attackRange;
        this.desirableRangeSquared = desirableRange * desirableRange;
        this.comeClose = comeClose;
        this.power = power;
        this.spread = spread;
        Check.argCondition(desirableRange > attackRange, "Desirable range can not exceed attack range!");
    }

    public Cooldown getCooldown() {
        return this.cooldown;
    }

    public void setProjectileGenerator(ProjectileGenerator projectileGenerator) {
        this.projectileGenerator = projectileGenerator;
    }

    public void setProjectileGenerator(Function<Entity, EntityProjectile> projectileGenerator) {
        this.projectileGenerator = (shooter, target, pow, spr) -> {
            EntityProjectile projectile = projectileGenerator.apply(shooter);
            projectile.setInstance(shooter.getInstance(), shooter.getPosition().add(0D, shooter.getEyeHeight(), 0D));
            projectile.shoot(target, pow, spr);
        };
    }

    private ProjectileGenerator getProjectileGeneratorOrDefault() {
        if (projectileGenerator == null) {
            setProjectileGenerator(shooter -> new EntityProjectile(shooter, EntityType.ARROW));
        }
        return projectileGenerator;
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
            if (!Cooldown.hasCooldown(time, this.lastShot, this.delay)) {
                if (this.entityCreature.hasLineOfSight(target)) {
                    final var to = target.getPosition().add(0D, target.getEyeHeight(), 0D);
                    this.getProjectileGeneratorOrDefault().shootProjectile(this.entityCreature, to, this.power, this.spread);

                    this.lastShot = time;
                } else {
                    comeClose = this.comeClose;
                }
            }
        }
        Navigator navigator = this.entityCreature.getNavigator();
        final var pathPosition = navigator.getPathPosition();
        if (!comeClose && distanceSquared <= this.desirableRangeSquared) {
            if (pathPosition != null) {
                navigator.setPathTo(null);
            }
            this.entityCreature.lookAt(target);
            return;
        }
        final var targetPosition = target.getPosition();
        if (pathPosition == null || !pathPosition.samePoint(targetPosition)) {
            if (this.cooldown.isReady(time)) {
                this.cooldown.refreshLastUpdate(time);
                navigator.setPathTo(targetPosition);
            }
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

    /**
     * The function used to generate a projectile.
     */
    public interface ProjectileGenerator {
        /**
         * Shoots a projectile.
         *
         * @param shooter the shooter.
         * @param target  the target position.
         * @param power   the shot power.
         * @param spread  the shot spread.
         */
        void shootProjectile(EntityCreature shooter, Pos target, double power, double spread);
    }
}
