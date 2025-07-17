package net.minestom.server.dialog;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DialogInputTest {
    
    /**
     * Tests for the validateKey(String key) method in the DialogInput class.
     * This method ensures that the input key only contains alphanumeric characters and underscores.
     * If invalid characters are found, it throws an IllegalArgumentException.
     */
    
    @Test
    void validateKey_validKey_doesNotThrow() {
        String validKey = "valid_key_123";
        assertDoesNotThrow(() -> DialogInput.validateKey(validKey));
    }
    
    @Test
    void validateKey_keyWithSpecialCharacters_throwsException() {
        String invalidKey = "invalid!key";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> DialogInput.validateKey(invalidKey));
        assertEquals("Invalid input key: invalid!key. Must match [a-zA-Z0-9_]+", exception.getMessage());
    }
    
    @Test
    void validateKey_keyWithSpaces_throwsException() {
        String keyWithSpaces = "key with spaces";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> DialogInput.validateKey(keyWithSpaces));
        assertEquals("Invalid input key: key with spaces. Must match [a-zA-Z0-9_]+", exception.getMessage());
    }

    @Test
    void validateKey_keyWithNumbersOnly_doesNotThrow() {
        String numericKey = "123456";
        assertDoesNotThrow(() -> DialogInput.validateKey(numericKey));
    }
    
    @Test
    void validateKey_keyWithUnderscoresOnly_doesNotThrow() {
        String underscoreKey = "_____";
        assertDoesNotThrow(() -> DialogInput.validateKey(underscoreKey));
    }
    
    @Test
    void validateKey_keyWithMixedValidCharacters_doesNotThrow() {
        String mixedKey = "key123_ABC";
        assertDoesNotThrow(() -> DialogInput.validateKey(mixedKey));
    }
}