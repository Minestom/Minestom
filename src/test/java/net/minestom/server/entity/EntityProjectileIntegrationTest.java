package net.minestom.server.entity;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.coordinate.Pos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class EntityProjectileIntegrationTest {
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

        var before = projectile.getPosition(); // at start
        var after = projectile.getPosition(); // now - 1 tick, closest to target
        var smallestDistance = 1e6;
        while (true) {
            final var distance = projectile.getPosition().distanceSquared(target);
            if (distance <= smallestDistance) smallestDistance = distance;
            else break;

            after = projectile.getPosition();
            env.tick();
        }

        // Ensure the position is correct.
        // x doesn't change
        // Big delta because ticks aren't very accurate
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

        var before = projectile.getPosition(); // at start
        var after = projectile.getPosition(); // now - 1 tick, closest to target
        var smallestDistance = 1e6;
        while (true) {
            final var distance = projectile.getPosition().distanceSquared(target);
            if (distance <= smallestDistance) smallestDistance = distance;
            else break;

            after = projectile.getPosition();
            env.tick();
        }

        // x and y don't change (no gravity) and z changes by Σz velocity.
        assertEquals(before.x(), after.x());
        assertEquals(before.y(), after.y());
        assertEquals(target.z(), after.z(), 0.05);
    }
}
