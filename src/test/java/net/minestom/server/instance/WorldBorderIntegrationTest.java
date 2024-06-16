package net.minestom.server.instance;

import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MicrotusExtension.class)
class WorldBorderIntegrationTest {

    @Test
    void setWorldborderSize(Env env) {
        Instance instance = env.createFlatInstance();

        instance.getWorldBorder().setDiameter(50.0);
        assertEquals(50.0, instance.getWorldBorder().getDiameter());
        instance.getWorldBorder().setDiameter(10.0);
        assertEquals(10.0, instance.getWorldBorder().getDiameter());
    }

    @Test
    void resizeWorldBorder(Env env) throws InterruptedException {
        Instance instance = env.createFlatInstance();

        instance.getWorldBorder().setDiameter(50.0);

        instance.getWorldBorder().setDiameter(10.0, 1);
        assertEquals(50.0, instance.getWorldBorder().getDiameter());

        Thread.sleep(10);
        instance.getWorldBorder().update();
        assertEquals(10.0, instance.getWorldBorder().getDiameter());
    }
}
