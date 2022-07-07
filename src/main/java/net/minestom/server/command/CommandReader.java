package net.minestom.server.command;

import java.nio.BufferUnderflowException;
import java.util.stream.IntStream;

//TODO IMPLEMENT METHODS
public final class CommandReader {
    static final char SPACE = ' ';
    static final char QUOTE = '"';
    static final char ESCAPE = '\\';
    private final CharSequence input;
    private int cursor = 0;

    public CommandReader(CharSequence input) {
        this.input = input;
    }

    public boolean hasRemaining() {
        return remaining() > 0;
    }

    public String getWord() {
        final int i = nextIndexOf(SPACE);
        return get(i == -1 ? input.length() : i);
    }

    public String getQuotedString() {
        if (getNextChar() != QUOTE) throw new RuntimeException("Tried to read an unquoted string as quoted.");
        int end;
        do {
            if (!hasRemaining()) throw new RuntimeException("Reached end of input before finding a closing quote");
            end = nextIndexOf(QUOTE);
            if (end == -1) throw new RuntimeException("Quoted string doesn't have a closing quote.");
        } while (getCharAt(end - 1) != ESCAPE);
        return get(end);
    }

    public String getRemaining() {
        return get(input.length());
    }

    public char getNextChar() {
        return getCharAt(cursor);
    }

    public char getCharAt(int position) {
        return input.charAt(position);
    }

    public void moveCursor(int amount) {
        cursor += amount;
    }

    public void consume() {

    }

    public void consume(int amount) {

    }

    public int getClosingIndexOfJsonObject() {
        return -1;
    }

    private String get(int end) {
        if (!hasRemaining()) throw new BufferUnderflowException();
        return input.subSequence(cursor, end).toString();
    }

    private int nextIndexOf(char c) {
        return IntStream.range(cursor, input.length()).filter(x -> input.charAt(x) == c).findFirst().orElse(-1);
    }

    public int remaining() {
        return input.length()-cursor;
    }
}
