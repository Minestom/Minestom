package net.minestom.testing.test;

import net.minestom.server.property.ServerProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EnvironmentTest {

    @Test
    void insideTest() {
        Assertions.assertTrue(ServerProperties.INSIDE_TEST.get());
    }
}
