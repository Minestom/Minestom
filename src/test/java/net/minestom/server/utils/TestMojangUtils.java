package net.minestom.server.utils;

import net.minestom.server.utils.mojang.MojangUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestMojangUtils {
    @Test
    public void testValidNameWorks() {
        var result = MojangUtils.fromUsername("jeb_");
        assertNotNull(result);
        assertEquals("jeb_", result.get("name").getAsString());
    }

    @Test
    public void testInvalidNameReturnsNull() {
        var result = MojangUtils.fromUsername("jfdsa84vvcxadubasdfcvn"); // Longer than 16, always invalid
        assertNull(result);
    }
}
