package net.minestom.server.instance;

import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MicrotusExtension.class)
class WorldBorderIntegrationTest {

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
    }
}
