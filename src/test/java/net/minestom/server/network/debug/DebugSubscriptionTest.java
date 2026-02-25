package net.minestom.server.network.debug;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DebugSubscriptionTest {

    @Test
    void testLookup() { // Bug when first introduced when the `DebugSubscriptions` was not loaded.
        assertNotNull(DebugSubscription.fromId(0)); // Possible case where we returned null and provoke a NPE.
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> assertNull(DebugSubscription.fromId(-1)));
    }
}
