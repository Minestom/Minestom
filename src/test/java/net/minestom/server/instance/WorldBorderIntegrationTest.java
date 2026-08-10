package net.minestom.server.instance;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class WorldBorderIntegrationTest {

    @Test
    public void setWorldborderSize(Env env) {
        Instance instance = env.createFlatInstance();

        instance.setWorldBorder(WorldBorder.DEFAULT_BORDER.withDiameter(50));
        assertEquals(50, instance.getWorldBorder().diameter());
        instance.setWorldBorder(WorldBorder.DEFAULT_BORDER.withDiameter(10));
        assertEquals(10, instance.getWorldBorder().diameter());
    }

    @Test
    public void resizeWorldBorder(Env env) {
        Instance instance = env.createFlatInstance();

        WorldBorder border = instance.getWorldBorder();
        instance.setWorldBorder(border.withDiameter(10));
        assertEquals(10, instance.getWorldBorder().diameter());

        // Lerp
        instance.setWorldBorder(border.withDiameter(30), 1);
        for (int i = 0; i < 10; i++) {
            assertEquals(10 + i, instance.getWorldBorder().diameter());
            instance.tick(0);
        }

        // Lerp from another diameter mid lerp
        instance.setWorldBorder(border.withDiameter(25), 0.25);
        for (int i = 0; i < 5; i++) {
            assertEquals(20 + i, instance.getWorldBorder().diameter());
            instance.tick(0);
        }

        // Ensure lerp finished
        for (int i = 0; i < 4; i++) {
            assertEquals(25, instance.getWorldBorder().diameter());
            instance.tick(0);
        }
    }

    @Test
    public void invalidArguments(Env env) {
        Instance instance = env.createFlatInstance();

        WorldBorder border = instance.getWorldBorder();
        assertThrows(IllegalStateException.class, () -> instance.setWorldBorder(border, -1));
        assertThrows(IllegalArgumentException.class, () -> border.withDiameter(-1));
        assertThrows(IllegalArgumentException.class, () -> border.withDiameter(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> border.withCenter(Double.NaN, 0));
        assertThrows(IllegalArgumentException.class, () -> border.withCenter(0, Double.NaN));
    }

    @Test
    public void pointInBoundsExcludesMaximumBound() {
        WorldBorder border = new WorldBorder(4, 0, 0, 0, 0);
        assertTrue(border.inBounds(new Vec(-2, 0, -2)));
        assertTrue(border.inBounds(new Vec(1.999, 0, 1.999)));
        assertFalse(border.inBounds(new Vec(2, 0, 0)));
        assertFalse(border.inBounds(new Vec(0, 0, 2)));
    }

    @Test
    public void entityBoundsIncludeBoundingBox(Env env) {
        Instance instance = env.createFlatInstance();
        WorldBorder border = new WorldBorder(4, 0, 0, 0, 0);
        Entity entity = new Entity(EntityType.ZOMBIE);
        double maximumEntityX = 2 - entity.getBoundingBox().maxX();
        entity.setInstance(instance, new Pos(maximumEntityX, 42, 0)).join();

        assertTrue(border.inBounds(entity.getPosition(), entity.getBoundingBox()));
        entity.refreshPosition(new Pos(maximumEntityX + Vec.EPSILON, 42, 0));
        assertFalse(border.inBounds(entity.getPosition(), entity.getBoundingBox()));
    }
}
