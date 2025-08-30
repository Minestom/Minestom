package net.minestom.server.entity.ai.goal;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.utils.time.Cooldown;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.function.Function;

public class RangedAttackGoal extends Goal {
    private long lastShot;
    private final Duration delay;
    private final int attackRangeSquared;
    private final double power;
    private final double spread;

    private @Nullable ProjectileGenerator projectileGenerator;

    /**
     * @param entityCreature the entity to add the goal to
     * @param delay          the delay between each shot
     * @param attackRange    the allowed range the entity can shoot others
     * @param spread         shot spread (0 for best accuracy)
     * @param power          shot power (1 for normal)
     * @param timeUnit       the unit of the delay
     */
    public RangedAttackGoal(EntityCreature entityCreature, int attackRange, double power, double spread, int delay, TemporalUnit timeUnit) {
        this(entityCreature, attackRange, power, spread, Duration.of(delay, timeUnit));
    }

    /**
     * @param entityCreature the entity to add the goal to
     * @param delay          the delay between each shots
     * @param attackRange    the allowed range the entity can shoot others
     * @param spread         shot spread (0 for best accuracy)
     * @param power          shot power (1 for normal)
     */
    public RangedAttackGoal(EntityCreature entityCreature, int attackRange, double power, double spread, Duration delay) {
        super(entityCreature);
        this.delay = delay;
        this.attackRangeSquared = attackRange * attackRange;
        this.power = power;
        this.spread = spread;
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
    public boolean canStart() {
        final Entity target = entityCreature.getAi().getTarget();
        return target != null && entityCreature.getDistance(target) <= attackRangeSquared;
    }

    @Override
    public void start() {

    }

    @Override
    public void tick(long time) {
        final Entity target = entityCreature.getAi().getTarget();
        assert target != null;

        if (!Cooldown.hasCooldown(time, lastShot, delay)) {
            if (entityCreature.hasLineOfSight(target)) {
                entityCreature.lookAt(target);

                final var to = target.getPosition().add(0D, target.getEyeHeight(), 0D);
                this.getProjectileGeneratorOrDefault().shootProjectile(entityCreature, to, power, spread);

                this.lastShot = time;
            }
        }
    }

    @Override
    public boolean shouldEnd() {
        final Entity target = entityCreature.getAi().getTarget();
        return target == null || entityCreature.getDistanceSquared(target) > attackRangeSquared;
    }

    @Override
    public void end() {

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
