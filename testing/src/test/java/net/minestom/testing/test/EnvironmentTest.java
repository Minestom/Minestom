package net.minestom.testing.test;

import net.minestom.server.ServerFlag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EnvironmentTest {

    @Test
    void insideTest() {
        Assertions.assertTrue(ServerFlag.INSIDE_TEST);
    }
}
