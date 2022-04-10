package net.minestom.server.entity;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.coordinate.Pos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class EntityViewDirectionTest {
    private static final float EPSILON = 0.01f;

    @Test
    public void viewYawAndPitch(Env env) {
        var instance = env.createFlatInstance();
        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 40, 0)).join();
        entity.setView(0, 0);
        assertEquals(0, entity.getPosition().yaw());
        assertEquals(0, entity.getPosition().pitch());

        entity.setView(90, 0);
        assertEquals(90, entity.getPosition().yaw());
        assertEquals(0, entity.getPosition().pitch());

        entity.setView(0, 42);
        assertEquals(0, entity.getPosition().yaw());
        assertEquals(42, entity.getPosition().pitch());

        entity.setView(37, 26);
        assertEquals(37, entity.getPosition().yaw());
        assertEquals(26, entity.getPosition().pitch());
    }

    @Test
    public void lookAtPos(Env env) {
        var instance = env.createFlatInstance();
        var entity = new Entity(EntityType.ZOMBIE);
        entity.setInstance(instance, new Pos(0, 40, 0)).join();

        entity.lookAt(new Pos(16, 40, 16));
        assertEquals(-45f, entity.getPosition().yaw());
        assertEquals(0f, entity.getPosition().pitch(), EPSILON);

        entity.lookAt(new Pos(-16, 40, 56));
        assertEquals(15.94f, entity.getPosition().yaw(), EPSILON);
        assertEquals(0f, entity.getPosition().pitch(), EPSILON);

        entity.lookAt(new Pos(48, 36, 48));
        assertEquals(-45f, entity.getPosition().yaw(), EPSILON);
        assertEquals(4.76f, entity.getPosition().pitch(), EPSILON);

        entity.lookAt(new Pos(48, 36, -17));
        assertEquals(-109.50f, entity.getPosition().yaw(), EPSILON);
        // should have the same pitch as the previous position
        assertEquals(4.76f, entity.getPosition().pitch(), EPSILON);

        entity.lookAt(new Pos(0, 87, 0));
        // looking from below, not checking the yaw
        assertEquals(-90f, entity.getPosition().pitch(), EPSILON);

        entity.lookAt(new Pos(-25, 42, 4));
        assertEquals(80.90f, entity.getPosition().yaw(), EPSILON);
        assertEquals(-4.57f, entity.getPosition().pitch(), EPSILON);
    }

    @Test
    public void lookAtEntitySameType(Env env) {
        var instance = env.createFlatInstance();
        // same type, same eye height
        var e1 = new Entity(EntityType.ZOMBIE);
        var e2 = new Entity(EntityType.ZOMBIE);
        e1.setInstance(instance, new Pos(0, 40, 0)).join();
        e2.setInstance(instance, new Pos(0, 50, 0)).join();

        e1.lookAt(e2);
        // e2 is above e1, the pich should be negative
        assertEquals(-90f, e1.getPosition().pitch(), EPSILON);

        e2.teleport(new Pos(0, 10, 0)).join();
        e1.lookAt(e2);
        // e2 is below e1, the pich should be positive
        assertEquals(90f, e1.getPosition().pitch(), EPSILON);

        e2.teleport(new Pos(16, 40, 16)).join();
        e1.lookAt(e2);
        assertEquals(-45f, e1.getPosition().yaw(), EPSILON);
        // e2 has the same y as e1, the pich should be 0
        assertEquals(0f, e1.getPosition().pitch(), EPSILON);
    }

    @Test
    public void lookAtEntityDifferentType(Env env) {
        var instance = env.createFlatInstance();
        // same type, same eye height
        var e1 = new Entity(EntityType.ZOMBIE);
        // a chicken has a lower eye height than a zombie
        var e2 = new Entity(EntityType.CHICKEN);
        e1.setInstance(instance, new Pos(0, 40, 0)).join();
        e2.setInstance(instance, new Pos(0, 40, 0)).join();

        e1.lookAt(e2);
        // e2 eyes are below e1, the pich should be positive
        assertEquals(90f, e1.getPosition().pitch(), EPSILON);

        double eyeDifference = e1.getEyeHeight() - e2.getEyeHeight();
        assertTrue(eyeDifference > 0);
        var pos = new Pos(10, e1.getPosition().y() + eyeDifference, 10);
        e2.teleport(pos).join();
        e1.lookAt(e2);
        // e2 eyes are at the same height as e1's, the pitch should be 0
        assertEquals(0f, e1.getPosition().pitch(), EPSILON);

        e2.teleport(new Pos(-16, 40, -16)).join();
        e1.lookAt(e2);
        assertEquals(135f, e1.getPosition().yaw(), EPSILON);
        assertEquals(3.79f, e1.getPosition().pitch(), EPSILON);

        e2.teleport(new Pos(8, 50, -32)).join();
        e1.lookAt(e2);
        assertEquals(-165.96f, e1.getPosition().yaw(), EPSILON);
        assertEquals(-15.60f, e1.getPosition().pitch(), EPSILON);

        e2.teleport(new Pos(0, 30, -2)).join();
        e1.lookAt(e2);
        assertEquals(-180f, e1.getPosition().yaw(), EPSILON);
        assertEquals(79.75f, e1.getPosition().pitch(), EPSILON);
    }
}
