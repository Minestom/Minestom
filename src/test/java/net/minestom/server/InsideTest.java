package net.minestom.server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class InsideTest {
    @Test
    void inside() {
        assertTrue(ServerFlag.INSIDE_TEST);
    }
}
