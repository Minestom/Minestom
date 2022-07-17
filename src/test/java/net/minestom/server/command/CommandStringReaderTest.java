package net.minestom.server.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class CommandStringReaderTest {
    @Test
    public void searchTest() {
        final CommandStringReader reader = new CommandStringReader("0123456789ABCDEF0123456789ABCDEF");
        assertEquals(3, reader.nextIndexOf('3', 0));
        assertEquals(1, reader.nextIndexOf('1', 0));
        assertEquals(12, reader.nextIndexOf('C', 0));
        assertEquals(19, reader.nextIndexOf('3', 15));
        assertEquals(17, reader.nextIndexOf('1', 15));
        assertEquals(28, reader.nextIndexOf('C', 15));
    }

    @Test
    public void readRaw() {
        final CommandStringReader reader = new CommandStringReader("0123456789ABCDEF");
        assertEquals("01", reader.read(2));
        assertEquals("23456", reader.read(7));
        assertEquals("7", reader.read(8));
        assertEquals("89ABCD", reader.read(14));
        assertEquals("EF", reader.read(16));
        assertFalse(reader.hasRemaining());
    }

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
