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

    @Test
    void fromStringDashed() {
        assertEquals(TEST_UUID, UUIDUtils.fromString("d2ac7139-76a6-435b-b659-7852d34dd7a3"));
    }

    @Test
    void fromStringDashless() {
        assertEquals(TEST_UUID, UUIDUtils.fromString("d2ac713976a6435bb6597852d34dd7a3"));
        assertEquals(UUID.fromString("ab70ecb4-2346-4c14-a52d-7a091507c24e"),
                UUIDUtils.fromString("ab70ecb423464c14a52d7a091507c24e"));
    }

    @Test
    void fromStringUppercase() {
        assertEquals(TEST_UUID, UUIDUtils.fromString("D2AC713976A6435BB6597852D34DD7A3"));
    }

    @Test
    void fromStringInvalid() {
        assertThrows(IllegalArgumentException.class, () -> UUIDUtils.fromString("not a uuid"));
        assertThrows(IllegalArgumentException.class, () -> UUIDUtils.fromString("d2ac713976a6435bb6597852d34dd7a"));
        assertThrows(IllegalArgumentException.class, () -> UUIDUtils.fromString("d2ac713976a6435bb6597852d34dd7a33"));
        assertThrows(IllegalArgumentException.class, () -> UUIDUtils.fromString("g2ac713976a6435bb6597852d34dd7a3"));
    }
}