package net.minestom.server;

import net.minestom.server.property.ServerProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class InsideTest {
    @Test
    public void inside() {
        assertTrue(ServerProperties.INSIDE_TEST.get());
    }
}
