package net.minestom.server.entity;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.coordinate.Pos;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class EntityProjectileIntegrationTest {
    private static final double EPSILON = 1.0E-08;
    private static final double MOVEMENT_EPSILON = 1.0E-03;

    @Test
    public void gravityVelocity(Env env) {
        var instance = env.createFlatInstance();
        var shooter = new EntityCreature(EntityType.SKELETON);
        shooter.setInstance(instance, new Pos(0, 42, 0)).join();
        var projectile = new EntityProjectile(shooter, EntityType.ARROW);
        var from = new Pos(0, 42, 0).add(0,
                shooter.getEyeHeight(), shooter.getPosition().direction().z());
        projectile.setInstance(instance, from).join();
        projectile.shoot(from.add(0, 0, 10), 1, 0);

        // z velocity = dz / sqrt(dx^2 + dy^2 + dz^2) * 20 * power =
        // 10 / sqrt(2.0000000298023224^2 + 10^2) * 20 * 1 = 10 / 10.198039033 * 20 * 1 = 19.6116135026.
        assertEquals(19.6116135026, projectile.velocity.z(), EPSILON);

        // The entity is currently falling (in the air), so it does have a velocity.
        assertTrue(projectile.hasVelocity());

        // Check whether gravity gets applied correctly
        testMovement(env, projectile, List.of(
                new Pos(0, 43.6915, 1),
                new Pos(0, 43.8876, 1.98058),
                new Pos(0, 44.0322, 2.94154),
                new Pos(0, 44.1259, 3.88329),
                new Pos(0, 44.1692, 4.80621),
                new Pos(0, 44.1625, 5.71067)
        ));
    }

    @Test
    public void noGravityVelocity(Env env) {
        var instance = env.createFlatInstance();
        var shooter = new EntityCreature(EntityType.SKELETON);
        shooter.setInstance(instance, new Pos(0, 42, 0)).join();
        var projectile = new EntityProjectile(shooter, EntityType.ARROW);
        var from = new Pos(0, 42, 0).add(0,
                shooter.getEyeHeight(), shooter.getPosition().direction().z());
        projectile.setNoGravity(true);
        projectile.setInstance(instance, from).join();
        projectile.shoot(from.add(0, 0, 10), 1, 0);

        // z velocity = dz / sqrt(dx^2 + dy^2 + dz^2) * 20 * power = 10 / 10 * 20 * 1 = 20.
        assertEquals(20, projectile.velocity.z());

        // The entity is currently falling (in the air), so it does have a velocity.
        assertTrue(projectile.hasVelocity());

        var before = projectile.getPosition();

        for (int i = 1; i <= 5; i++) {
            env.tick();

            // Only drag for non-living entities should apply since there is no gravity .
            // We do this by multiplying by 0.98, so 20 * 0.98^n.
            assertEquals(0, projectile.velocity.x());
            assertEquals(0, projectile.velocity.y());
            assertEquals(20 * Math.pow(0.98, i), projectile.velocity.z(), EPSILON);
        }

        // Ensure the position is correct.
        // x and y don't change (no gravity) and z changes by Σz velocity.
        var after = projectile.getPosition();
        assertEquals(before.x(), after.x());
        assertEquals(before.y(), after.y());
        assertEquals(before.z() + zAfterNoGravity(5), after.z(), EPSILON);
    }

    private void testMovement(Env env, Entity entity, List<Pos> samples) {
        for (Pos sample : samples) {
            var position = entity.getPosition();
            assertEquals(sample.x(), position.x(), MOVEMENT_EPSILON);
            assertEquals(sample.y(), position.y(), MOVEMENT_EPSILON);
            assertEquals(sample.z(), position.z(), MOVEMENT_EPSILON);
            env.tick();
        }
    }

    private static double zAfterNoGravity(int ticks) {
        return IntStream.range(0, ticks)
                .mapToDouble(i -> Math.pow(0.98, i))
                .sum();
    }
}
