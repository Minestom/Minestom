package net.minestom.server.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class CommandReaderTest {
    @Test
    public void searchTest() {
        final CommandReader reader = new CommandReader("0123456789ABCDEF0123456789ABCDEF");
        assertEquals(3, reader.nextIndexOf('3', 0));
        assertEquals(1, reader.nextIndexOf('1', 0));
        assertEquals(12, reader.nextIndexOf('C', 0));
        assertEquals(19, reader.nextIndexOf('3', 15));
        assertEquals(17, reader.nextIndexOf('1', 15));
        assertEquals(28, reader.nextIndexOf('C', 15));
    }

    @Test
    public void readRaw() {
        final CommandReader reader = new CommandReader("0123456789ABCDEF");
        assertEquals("01", reader.read(2));
        assertEquals("23456", reader.read(7));
        assertEquals("7", reader.read(8));
        assertEquals("89ABCD", reader.read(14));
        assertEquals("EF", reader.read(16));
        assertFalse(reader.hasRemaining());
    }

    @Test
    public void readWords() {
        final CommandReader reader = new CommandReader("test 15 foo bar a");
        assertEquals("test", reader.readWord());
        assertEquals("15", reader.readWord());
        assertEquals("foo", reader.readWord());
        assertEquals("bar", reader.readWord());
        assertEquals("a", reader.readWord());
        assertFalse(reader.hasRemaining());
    }

    @Test
    public void readQuotedStrings() {
        final CommandReader reader = new CommandReader("\"test 15\" \"foo \\\\\"bar\" \"a\"");
        assertEquals("test 15", reader.readQuotedString());
        assertEquals("foo \\\"bar", reader.readQuotedString());
        assertEquals("a", reader.readQuotedString());
        assertFalse(reader.hasRemaining());
    }

    @Test
    public void readWordsAndQuotedStringsMixed() {
        final CommandReader reader = new CommandReader("\"te\\\"st\" 15 foo \"bar\" \"\\\"a\\\"\"");
        assertEquals("te\"st", reader.readQuotedString());
        assertEquals("15", reader.readWord());
        assertEquals("foo", reader.readWord());
        assertEquals("bar", reader.readQuotedString());
        assertEquals("\"a\"", reader.readQuotedString());
        assertFalse(reader.hasRemaining());
    }

    @Test
    public void getJsonObjectClosingIndex() {
        assertEquals(-1, jsonTest("no json here"));
        assertEquals(-1, jsonTest("no valid {json here"));
        assertEquals(1, jsonTest("{}"));
        assertEquals(8, jsonTest("0123456{}9A"));
        assertEquals(11, jsonTest("012{\"foo\":5}+"));
        assertEquals(76, jsonTest("{\"foo\":1,\"bar\":\"S9 M  M\",\"baz\":false,\"array\":[],\"obj\":{\"foo\":\"fq h{q}{{{r}\"}}"));
        assertEquals(82, jsonTest("012345{\"foo\":1,\"bar\":\"S9 M  M\",\"baz\":false,\"array\":[],\"obj\":{\"foo\":\"fq h{q}{{{r}\"}}789"));
    }

    private int jsonTest(String input) {
        return new CommandReader(input).getClosingIndexOfJsonObject(0);
    }
}
