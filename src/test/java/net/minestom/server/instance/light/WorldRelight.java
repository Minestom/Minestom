package net.minestom.server.instance.light;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import org.junit.jupiter.api.Test;

@EnvTest
public class WorldRelight {

    @Test
    public void testBlockRemoval(Env env) {
        env.createLightingInstance();
    }
}
