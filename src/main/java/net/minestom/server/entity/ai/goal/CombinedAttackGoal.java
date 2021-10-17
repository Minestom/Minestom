package net.minestom.server.entity.ai.goal;

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

/**
 * Allows entity to perform both melee and ranged attacks.
 */
public class CombinedAttackGoal extends GoalSelector {

    private final Cooldown cooldown = new Cooldown(Duration.of(5, TimeUnit.SERVER_TICK));

    private final int meleeRangeSquared;
    private final Duration meleeDelay;
    private final int rangedRangeSquared;
    private final double rangedPower;
    private final double rangedSpread;
    private final Duration rangedDelay;
    private final int desirableRangeSquared;
    private final boolean comeClose;

    private Function<Entity, EntityProjectile> projectileGenerator;

    private long lastAttack;
    private boolean stop;
    private Entity cachedTarget;

    /**
     * @param entityCreature the entity to add the goal to.
     * @param meleeRange     the allowed range the entity can hit others in melee.
     * @param rangedRange    the allowed range the entity can shoot others.
     * @param rangedPower    shot power (1 for normal).
     * @param rangedSpread   shot spread (0 for best accuracy).
     * @param delay          the delay between any attacks.
     * @param timeUnit       the unit of the delay.
     * @param desirableRange the desirable range: the entity will try to stay no further than this distance.
     * @param comeClose      if entity should go as close as possible to the target whether target is not in line of sight for a ranged attack.
     */
    public CombinedAttackGoal(@NotNull EntityCreature entityCreature,
                              int meleeRange, int rangedRange, double rangedPower, double rangedSpread,
                              int delay, TemporalUnit timeUnit,
                              int desirableRange, boolean comeClose) {
        this(
                entityCreature,
                meleeRange, delay, timeUnit,
                rangedRange, rangedPower, rangedSpread, delay, timeUnit,
                desirableRange, comeClose
        );
    }

    /**
     * @param entityCreature the entity to add the goal to.
     * @param meleeRange     the allowed range the entity can hit others in melee.
     * @param rangedRange    the allowed range the entity can shoot others.
     * @param rangedPower    shot power (1 for normal).
     * @param rangedSpread   shot spread (0 for best accuracy).
     * @param delay          the delay between any attacks.
     * @param desirableRange the desirable range: the entity will try to stay no further than this distance.
     * @param comeClose      if entity should go as close as possible to the target whether target is not in line of sight for a ranged attack.
     */
    public CombinedAttackGoal(@NotNull EntityCreature entityCreature,
                              int meleeRange, int rangedRange, double rangedPower, double rangedSpread,
                              Duration delay,
                              int desirableRange, boolean comeClose) {
        this(
                entityCreature,
                meleeRange, delay,
                rangedRange, rangedPower, rangedSpread, delay,
                desirableRange, comeClose
        );
    }

    /**
     * @param entityCreature the entity to add the goal to.
     * @param meleeRange     the allowed range the entity can hit others in melee.
     * @param meleeDelay     the delay between melee attacks.
     * @param meleeTimeUnit  the unit of the melee delay.
     * @param rangedRange    the allowed range the entity can shoot others.
     * @param rangedPower    shot power (1 for normal).
     * @param rangedSpread   shot spread (0 for best accuracy).
     * @param rangedDelay    the delay between ranged attacks.
     * @param rangedTimeUnit the unit of the ranged delay.
     * @param desirableRange the desirable range: the entity will try to stay no further than this distance.
     * @param comeClose      if entity should go as close as possible to the target whether target is not in line of sight for a ranged attack.
     */
    public CombinedAttackGoal(@NotNull EntityCreature entityCreature,
                              int meleeRange, int meleeDelay, TemporalUnit meleeTimeUnit,
                              int rangedRange, double rangedPower, double rangedSpread, int rangedDelay, TemporalUnit rangedTimeUnit,
                              int desirableRange, boolean comeClose) {
        this(entityCreature, meleeRange, Duration.of(meleeDelay, meleeTimeUnit), rangedRange, rangedPower, rangedSpread,
                Duration.of(rangedDelay, rangedTimeUnit), desirableRange, comeClose);
    }

    /**
     * @param entityCreature the entity to add the goal to.
     * @param meleeRange     the allowed range the entity can hit others in melee.
     * @param meleeDelay     the delay between melee attacks.
     * @param rangedRange    the allowed range the entity can shoot others.
     * @param rangedPower    shot power (1 for normal).
     * @param rangedSpread   shot spread (0 for best accuracy).
     * @param rangedDelay    the delay between ranged attacks.
     * @param desirableRange the desirable range: the entity will try to stay no further than this distance.
     * @param comeClose      if entity should go as close as possible to the target whether target is not in line of sight for a ranged attack.
     */
    public CombinedAttackGoal(@NotNull EntityCreature entityCreature,
                              int meleeRange, Duration meleeDelay,
                              int rangedRange, double rangedPower, double rangedSpread, Duration rangedDelay,
                              int desirableRange, boolean comeClose) {
        super(entityCreature);
        this.meleeRangeSquared = meleeRange * meleeRange;
        this.meleeDelay = meleeDelay;
        this.rangedRangeSquared = rangedRange * rangedRange;
        this.rangedPower = rangedPower;
        this.rangedSpread = rangedSpread;
        this.rangedDelay = rangedDelay;
        this.desirableRangeSquared = desirableRange * desirableRange;
        this.comeClose = comeClose;
        Check.argCondition(desirableRange > rangedRange, "Desirable range can not exceed ranged range!");
    }

    public Cooldown getCooldown() {
        return this.cooldown;
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
        // First of all, checking if to perform melee or ranged attack depending on the distance to target.
        if (distanceSquared <= this.meleeRangeSquared) {
            if (!Cooldown.hasCooldown(time, this.lastAttack, this.meleeDelay)) {
                this.entityCreature.attack(target, true);
                this.lastAttack = time;
            }
        } else if (distanceSquared <= this.rangedRangeSquared) {
            if (!Cooldown.hasCooldown(time, this.lastAttack, this.rangedDelay)) {
                if (this.entityCreature.hasLineOfSight(target)) {
                    // If target is on line of entity sight, ranged attack can be performed
                    final var to = target.getPosition().add(0D, target.getEyeHeight(), 0D);

                    Function<Entity, EntityProjectile> projectileGenerator = this.projectileGenerator;
                    if (projectileGenerator == null) {
                        projectileGenerator = shooter -> new EntityProjectile(shooter, EntityType.ARROW);
                    }
                    EntityProjectile projectile = projectileGenerator.apply(this.entityCreature);
                    projectile.setInstance(this.entityCreature.getInstance(), this.entityCreature.getPosition());

                    projectile.shoot(to, this.rangedPower, this.rangedSpread);
                    this.lastAttack = time;
                } else {
                    // Otherwise deciding whether to go to the enemy.
                    comeClose = this.comeClose;
                }
            }
        }
        Navigator navigator = this.entityCreature.getNavigator();
        final var pathPosition = navigator.getPathPosition();
        // If we don't want to come close and we're already within desirable range, no movement is needed.
        if (!comeClose && distanceSquared <= this.desirableRangeSquared) {
            if (pathPosition != null) {
                navigator.setPathTo(null);
            }
            this.entityCreature.lookAt(target);
            return;
        }
        // Otherwise going to the target.
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

}
