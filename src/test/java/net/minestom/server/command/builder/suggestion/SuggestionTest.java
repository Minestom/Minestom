package net.minestom.server.command.builder.suggestion;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class SuggestionTest {
    @Test
    void simpleRange() {
        assertEquals("test", new Suggestion("test", 0, 4).getCurrent());
    }

    @Test
    void rangeOverflowThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Suggestion("test", 0, 5));
    }

    @Test
    void rangeUnderflowThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Suggestion("test", -1, 5));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    void zeroLengthSubstring(int index) {
        assertEquals(0, new Suggestion("test", index, 0).getCurrent().length());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    void singleLengthSubstring(int index) {
        String value = "test";
        assertEquals(Character.toString(value.charAt(index)), new Suggestion(value, index, 1).getCurrent());
    }
}