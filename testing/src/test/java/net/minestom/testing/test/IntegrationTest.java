package net.minestom.testing.test;

import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@EnvTest
public class IntegrationTest {

    @Test
    void testEnv(Env env) {
        Assertions.assertNotNull(env);
        Assertions.assertNotNull(env.process());
    }
}
