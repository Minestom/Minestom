package net.minestom.server;

import net.minestom.server.utils.debug.DebugUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class InsideTest {
    @Test
    void inside() {
        assertTrue(DebugUtils.INSIDE_TEST);
    }
}
