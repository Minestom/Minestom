package net.minestom.server.utils;

import net.minestom.server.utils.mojang.MojangUtils;
import net.minestom.server.utils.mojang.ServiceNotAvailableException;
import net.minestom.server.utils.mojang.UsernameDoesNotExistException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TestMojangUtils {
    private final UUID JEB_UUID = UUID.fromString("853c80ef-3c37-49fd-aa49-938b674adae6");
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

    @Test
    public void testValidUuidWorks() {
        var result = MojangUtils.fromUuid(JEB_UUID.toString());
        assertNotNull(result);
        assertEquals("jeb_", result.get("name").getAsString());
        assertEquals("853c80ef3c3749fdaa49938b674adae6", result.get("id").getAsString());
    }

    @Test
    public void testInvalidUuidReturnsNull() {
        var result = MojangUtils.fromUuid("853c80ef3c3749fdaa49938b674adae6a"); // Longer than 32, always invalid
        assertNull(result);
    }

    @Test
    public void testNonExistentUuidReturnsNull() {
        var result = MojangUtils.fromUuid("00000000-0000-0000-0000-000000000000");
        assertNull(result);
    }

    @Test
    public void testValidUUIDWorks() {
        var result = MojangUtils.fromUuid(JEB_UUID);
        assertNotNull(result);
        assertEquals("jeb_", result.get("name").getAsString());
        assertEquals("853c80ef3c3749fdaa49938b674adae6", result.get("id").getAsString());
    }

    @Test
    public void testGetValidNameWorks() throws ServiceNotAvailableException, UsernameDoesNotExistException {
        assertEquals(JEB_UUID, MojangUtils.getUUID("jeb_"));
    }

    @Test
    public void testGetValidUUIDWorks() throws ServiceNotAvailableException, UsernameDoesNotExistException {
        assertEquals("jeb_", MojangUtils.getUsername(JEB_UUID));
    }

    @Test
    public void testGetInvalidNameThrows() {
        assertThrows(UsernameDoesNotExistException.class, () -> MojangUtils.getUUID("a")); // Too short
    }
}
