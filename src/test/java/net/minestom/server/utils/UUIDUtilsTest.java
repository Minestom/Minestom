package net.minestom.server.utils;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UUIDUtilsTest {
    private static final UUID TEST_UUID = UUID.fromString("d2ac7139-76a6-435b-b659-7852d34dd7a3");
    private static final int[] TEST_INT_ARRAY = new int[]{
            0xd2ac7139,
            0x76a6435b,
            0xb6597852,
            0xd34dd7a3
    };

    @Test
    void isUuid() {
        assertTrue(UUIDUtils.isUuid("d2ac7139-76a6-435b-b659-7852d34dd7a3"));
        assertFalse(UUIDUtils.isUuid("This is not a UUID"));
        assertFalse(UUIDUtils.isUuid("d2acL139-76a6-435b-b659-7852d34dd7a3"));
    }

    @Test
    void uuidToIntArray() {
        assertArrayEquals(TEST_INT_ARRAY, UUIDUtils.uuidToIntArray(TEST_UUID));
    }

    @Test
    void intArrayToUuid() {
        assertEquals(TEST_UUID, UUIDUtils.intArrayToUuid(TEST_INT_ARRAY));
    }
}