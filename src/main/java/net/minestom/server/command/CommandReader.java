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

    public CommandReader(CharSequence input) {
        this.input = input;
    }

    public boolean hasRemaining() {
        return remaining() > 0;
    }

    public String readWord() {
        return readUntil(SPACE);
    }

    //fixme single quotes are also valid
    public String readQuotedString() {
        if (peekNextChar() != QUOTE) throw new RuntimeException("Tried to read an unquoted string as quoted.");
        int end = cursor;
        do {
            end = nextIndexOf(QUOTE, end-cursor+1);
            if (end == -1) throw new RuntimeException("Quoted string doesn't have a closing quote.");
        } while (getCharAt(end - 1) == ESCAPE);
        final String s = read(end+1);
        cursor++;
        return s.substring(1, s.length()-1).replaceAll("\\\\\"", "\"");
    }

    public String readRemaining() {
        return read(input.length());
    }

    public char peekNextChar() {
        return getCharAt(cursor);
    }

    public char getCharAt(int position) {
        return input.charAt(position);
    }

    public int cursor() {
        return cursor;
    }

    public void setCursor(int cursor) {
        this.cursor = cursor;
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

    public int getClosingIndexOfJsonArray(int fromOffset) {
        int count = 1;
        boolean insideString = false;
        boolean lastWasEscape = false;
        final int start = nextIndexOf('[', fromOffset);
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
                if (current == '[')
                    count++;
                else if (current == ']' && --count == 0)
                    return i;
                else if (current == '"')
                    insideString = true;
            }
        }
        return -1;
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

    public String readUntilAny(char ...c) {
        int end = -1;
        for (char c1 : c) {
            final int i1 = nextIndexOf(c1, 0);
            if (i1 != -1 && (end == -1 || i1 < end)) {
                end = i1;
            }
        }
        final String read = read(end == -1 ? input.length() : end);
        cursor++; // skip target char
        return read;
    }

    @VisibleForTesting
    int nextIndexOf(char c, int offset) {
        return IntStream.range(cursor+offset, input.length()).filter(x -> input.charAt(x) == c).findFirst().orElse(-1);
    }

    public int remaining() {
        return input.length()-cursor;
    }
}
