package net.minestom.server.utils.mojang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MojangUtilsTest {

    @Test
    public void validUsernames() {
        assertTrue(MojangUtils.isValidUsername("Notch"));
        assertTrue(MojangUtils.isValidUsername("jeb_"));
        // Accounts created before the 3-character minimum was introduced
        assertTrue(MojangUtils.isValidUsername("ab"));
        assertTrue(MojangUtils.isValidUsername("a"));
        // 16 characters, the modern maximum
        assertTrue(MojangUtils.isValidUsername("abcdefghijklmnop"));
    }

    @Test
    public void invalidUsernames() {
        assertFalse(MojangUtils.isValidUsername(""));
        // Longer than the 16-character maximum
        assertFalse(MojangUtils.isValidUsername("abcdefghijklmnopq"));
        // Illegal characters, must not reach the URL
        assertFalse(MojangUtils.isValidUsername("Notch?unsigned=true"));
        assertFalse(MojangUtils.isValidUsername("Notch/../admin"));
        assertFalse(MojangUtils.isValidUsername("Notch<script>"));
        assertFalse(MojangUtils.isValidUsername("名字"));
    }
}
