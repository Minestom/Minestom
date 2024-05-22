package net.minestom.server.utils;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static net.minestom.server.utils.UniqueIdUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class UniqueIdUtilsTest {

    @Test
    void testUniqueIDCheck() {
        assertFalse(isUniqueId(""));
        assertTrue(isUniqueId(UUID.randomUUID().toString()));
    }
}
