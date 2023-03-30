package net.minestom.server;

import net.minestom.server.utils.debug.DebugUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class InsideTest {
    @Test
    public void inside() {
        assertTrue(DebugUtils.INSIDE_TEST);
    }
}
