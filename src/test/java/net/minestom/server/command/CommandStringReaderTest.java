package net.minestom.server.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class CommandStringReaderTest {
    @Test
    public void readWords() {
        final CommandStringReader reader = new CommandStringReader("test 15 foo bar a");
        assertEquals("test", reader.readWord());
        assertEquals("15", reader.readWord());
        assertEquals("foo", reader.readWord());
        assertEquals("bar", reader.readWord());
        assertEquals("a", reader.readWord());
        assertFalse(reader.hasRemaining());
    }
}
