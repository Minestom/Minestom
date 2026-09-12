package net.minestom.server.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StringUtilsTest {
    @Test
    public void validUsernames() {
        assertTrue(StringUtils.isValidUsername("Notch"));
        assertTrue(StringUtils.isValidUsername("jeb_"));
        assertTrue(StringUtils.isValidUsername("abcdefghijklmnop"));
    }

    @Test
    public void invalidUsernames() {
        assertFalse(StringUtils.isValidUsername("Notch with spaces"));
        assertFalse(StringUtils.isValidUsername("Notch\u007F"));
        assertFalse(StringUtils.isValidUsername("Notch\n"));
        assertFalse(StringUtils.isValidUsername("水跃鱼"));
    }

    @Test
    public void emptyUsername() {
        assertFalse(StringUtils.isValidUsername(""));
    }

    @Test
    public void veryShortUsernames() {
        assertTrue(StringUtils.isValidUsername("a"));
        assertTrue(StringUtils.isValidUsername("ab"));
    }

    @Test
    public void veryLongUsernames() {
        assertFalse(StringUtils.isValidUsername("abcdefghijklmnopq"));
    }
}
