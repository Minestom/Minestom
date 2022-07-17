package net.minestom.server.command;

import org.jetbrains.annotations.VisibleForTesting;

import java.nio.BufferUnderflowException;
import java.util.stream.IntStream;

final class CommandStringReader {
    static final char SPACE = ' ';
    private final CharSequence input;
    private int cursor = 0;

    public CommandStringReader(CharSequence input) {
        this.input = input;
    }

    public boolean hasRemaining() {
        return remaining() > 0;
    }

    public String readWord() {
        return readUntil(SPACE);
    }

    public String readRemaining() {
        return read(input.length());
    }

    public int cursor() {
        return cursor;
    }

    public void setCursor(int cursor) {
        this.cursor = cursor;
    }

    public String read(int exclusiveAbsoluteEnd) {
        if (!hasRemaining()) throw new BufferUnderflowException();
        final String s = input.subSequence(cursor, exclusiveAbsoluteEnd).toString();
        cursor += s.length();
        return s;
    }

    /**
     * Reads until the supplied character or end of input is encountered, target char
     * will not be included in the result, but the cursor will skip it
     *
     * @param c end char
     * @return string from current position until end char
     */
    public String readUntil(char c) {
        final int i = nextIndexOf(c, 0);
        final String read = read(i == -1 ? input.length() : i);
        cursor++; // skip target char
        return read;
    }

    @VisibleForTesting
    int nextIndexOf(char c, int offset) {
        return IntStream.range(cursor + offset, input.length()).filter(x -> input.charAt(x) == c).findFirst().orElse(-1);
    }

    public int remaining() {
        return input.length() - cursor;
    }
}
