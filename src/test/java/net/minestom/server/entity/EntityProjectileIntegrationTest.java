package net.minestom.server.entity;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class EntityProjectileIntegrationTest {
    private static final double EPSILON = 1.0E-08;

    @Test
    public void gravityVelocity(Env env) {
        var instance = env.createFlatInstance();
        var shooter = new EntityCreature(EntityType.SKELETON);
        shooter.setInstance(instance, new Pos(0, 42, 0)).join();
        var projectile = new EntityProjectile(shooter, EntityType.ARROW);
        var from = new Pos(0, 42, 0).add(0,
                shooter.getEyeHeight(), shooter.getPosition().direction().z());
        var target = from.add(0, 0, 10);
        projectile.setInstance(instance, from).join();
        projectile.shoot(target, 1, 0);

        // z velocity = dz / sqrt(dx^2 + dy^2 + dz^2) * 20 * power =
        // 10 / sqrt(2.0000000298023224^2 + 10^2) * 20 * 1 = 10 / 10.198039033 * 20 * 1 = 19.6116135026.
        assertEquals(19.6116135026, projectile.velocity.z(), EPSILON);

        // The entity is currently falling (in the air), so it does have a velocity.
        assertTrue(projectile.hasVelocity());

        var before = projectile.getPosition();

        for (int i = 0; i < 11; i++) {
            Vec expected = projectile.velocity
                    .div(20)
                    .apply((x, y, z) -> new Vec(
                        x * 0.98,
                        (y - 0.05) * (1 - 0.01),
                        z * 0.98
                    ))
                    .mul(20);

            // First tick has to be env tick, then entity ticks will work
            if (i == 0) env.tick();
            else projectile.tick(0);

            assertEquals(0, projectile.velocity.x());
            assertEquals(expected.y(), projectile.velocity.y());
            assertEquals(expected.z(), projectile.velocity.z());
        }

        // Ensure the position is correct.
        // x doesn't change
        // Big delta because ticks aren't very accurate
        var after = projectile.getPosition();
        assertEquals(before.x(), after.x());
        assertEquals(target.y(), after.y(), 0.6);
        assertEquals(target.z(), after.z(), 0.6);
    }

    @Test
    public void noGravityVelocity(Env env) {
        var instance = env.createFlatInstance();
        var shooter = new EntityCreature(EntityType.SKELETON);
        shooter.setInstance(instance, new Pos(0, 42, 0)).join();
        var projectile = new EntityProjectile(shooter, EntityType.ARROW);
        var from = new Pos(0, 42, 0).add(0,
                shooter.getEyeHeight(), shooter.getPosition().direction().z());
        var target = from.add(0, 0, 10);
        projectile.setNoGravity(true);
        projectile.setInstance(instance, from).join();
        projectile.shoot(target, 1, 0);

        // z velocity = dz / sqrt(dx^2 + dy^2 + dz^2) * 20 * power = 10 / 10 * 20 * 1 = 20.
        assertEquals(20, projectile.velocity.z());

        // The entity is currently falling (in the air), so it does have a velocity.
        assertTrue(projectile.hasVelocity());

        var before = projectile.getPosition();

        for (int i = 0; i < 11; i++) {
            // Only drag for non-living entities should apply since there is no gravity.
            // We do this by multiplying by 0.98, so 20 * 0.98^n.
            assertEquals(0, projectile.velocity.x());
            assertEquals(0, projectile.velocity.y());
            assertEquals(20 * Math.pow(0.98, i), projectile.velocity.z(), EPSILON);

            // First tick has to be env tick, then entity ticks will work
            if (i == 0) env.tick();
            else projectile.tick(0);
        }

        // Ensure the position is correct.
        // x and y don't change (no gravity) and z changes by Σz velocity.
        var after = projectile.getPosition();
        assertEquals(before.x(), after.x());
        assertEquals(before.y(), after.y());
        assertEquals(before.z() + zAfterNoGravity(11), after.z(), EPSILON);
        assertEquals(target.z(), after.z(), 0.05);
    }

    private static double zAfterNoGravity(int ticks) {
        return IntStream.range(0, ticks)
                .mapToDouble(i -> Math.pow(0.98, i))
                .sum();
    }
}
