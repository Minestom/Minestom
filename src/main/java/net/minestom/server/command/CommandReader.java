package net.minestom.server.command;

import org.jetbrains.annotations.VisibleForTesting;

import java.nio.BufferUnderflowException;
import java.util.stream.IntStream;

public final class CommandReader {
    static final char SPACE = ' ';
    static final char QUOTE = '"';
    static final char ESCAPE = '\\';
    private final CharSequence input;
    private int cursor = 0;
    private int pendingData = 0;

    public CommandReader(CharSequence input) {
        this.input = input;
    }

    public boolean hasRemaining() {
        return remaining() > 0;
    }

    public String getWord() {
        final int i = nextIndexOf(SPACE, 0);
        final String s = get(i == -1 ? input.length() : i);
        pendingData = s.length()+1;
        return s;
    }

    public String getQuotedString() {
        if (getNextChar() != QUOTE) throw new RuntimeException("Tried to read an unquoted string as quoted.");
        int end = cursor;
        do {
            end = nextIndexOf(QUOTE, end-cursor+1);
            if (end == -1) throw new RuntimeException("Quoted string doesn't have a closing quote.");
        } while (getCharAt(end - 1) == ESCAPE);
        final String s = get(end+1);
        pendingData = s.length()+1;
        return s.substring(1, s.length()-1).replaceAll("\\\\\"", "\"");
    }

    public String getRemaining() {
        return get(input.length());
    }

    public char getNextChar() {
        return getCharAt(cursor);
    }

    public char getCharAt(int position) {
        pendingData = 1;
        return input.charAt(position);
    }

    public void consume() {
        cursor += pendingData;
    }

    public void consume(int amount) {
        cursor += amount;
    }

    public int getClosingIndexOfJsonObject(int fromOffset) {
        int count = 1;
        boolean insideString = false;
        boolean lastWasEscape = false;
        final int start = nextIndexOf('{', fromOffset);
        if (start == -1) return -1;
        for (int i = start+1; i < input.length(); i++) {
            final char current = getCharAt(i);
            if (insideString) {
                if (current == '"')
                    if (lastWasEscape)
                        lastWasEscape = false;
                    else
                        insideString = false;
                else if (current == '\\')
                    lastWasEscape = !lastWasEscape;
                else
                    lastWasEscape = false;
            } else {
                if (current == '{')
                    count++;
                else if (current == '}' && --count == 0)
                    return i;
                else if (current == '"')
                    insideString = true;
            }
        }
        return -1;
    }

    public String get(int exclusiveAbsoluteEnd) {
        if (!hasRemaining()) throw new BufferUnderflowException();
        final String s = input.subSequence(cursor, exclusiveAbsoluteEnd).toString();
        pendingData = s.length();
        return s;
    }

    @VisibleForTesting
    int nextIndexOf(char c, int offset) {
        return IntStream.range(cursor+offset, input.length()).filter(x -> input.charAt(x) == c).findFirst().orElse(-1);
    }

    public int remaining() {
        return input.length()-cursor;
    }
}
