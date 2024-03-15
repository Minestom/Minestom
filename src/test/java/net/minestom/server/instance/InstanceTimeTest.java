package net.minestom.server.instance;

import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@EnvTest
public class InstanceTimeTest {
    @Test
    public void testNoUpdatesWhenZeroTimeRate(Env env) {
        var instance = env.createFlatInstance();
        instance.setTimeRate(0);

        Assertions.assertNull(instance.getTimeUpdate());
    }
}
