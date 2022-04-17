package net.minestom.server.instance;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class WorldBorderIntegrationTest {

    @Test
    public void setWorldborderSize(Env env) {
        Instance instance = env.createFlatInstance();

        instance.getWorldBorder().setDiameter(50.0);
        assertEquals(50.0, instance.getWorldBorder().getDiameter());
        instance.getWorldBorder().setDiameter(10.0);
        assertEquals(10.0, instance.getWorldBorder().getDiameter());
    }

    @Test
    public void resizeWorldBorder(Env env) {
        Instance instance = env.createFlatInstance();

        instance.getWorldBorder().setDiameter(50.0);

        instance.getWorldBorder().setDiameter(10.0, 1);
        assertEquals(50.0, instance.getWorldBorder().getDiameter());

        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        instance.getWorldBorder().update();
                        assertEquals(10.0, instance.getWorldBorder().getDiameter());
                    }
                },
                2
        );
    }
}
