package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.builder.exception.ArgumentSyntaxException;

public class ArgumentReader {
    private static final char DOUBLE_QUOTES = '"';
    private static final char SINGLE_QUOTE = '\'';
    private static final char SYNTAX_ESCAPE = '\\';

    public final CharCondition SINGLE_QUOTED_STRING_END = value -> value == SINGLE_QUOTE && peekBackward() != SYNTAX_ESCAPE;
    public final CharCondition DOUBLE_QUOTED_STRING_END = value -> value == DOUBLE_QUOTES && peekBackward() != SYNTAX_ESCAPE;

    private final String arguments;
    private int cursorPosition;

    public ArgumentReader(String arguments) {
        this.arguments = arguments;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Basic methods
    ///////////////////////////////////////////////////////////////////////////

    public int getCursorPosition() {
        return cursorPosition;
    }

    /**
     * Move around the cursor
     * @param cursorPosition the new position
     */
    public void setCursorPosition(int cursorPosition) {
        if (cursorPosition < 0) {
            throw new IllegalArgumentException("Cursor position cannot be less than zero!");
        }
        this.cursorPosition = cursorPosition;
    }

    public boolean canRead() {
        return cursorPosition < arguments.length() - 1;
    }

    /**
     * Reads the character at the cursor and increments the position by one
     * @return the character at the cursor position
     */
    public char read() {
        return arguments.charAt(cursorPosition++);
    }

    /**
     * Moves the cursor position forward by one
     */
    public void skip() {
        cursorPosition++;
    }

    /**
     * Moves the cursor position back by one
     */
    public void back() {
        cursorPosition--;
    }

    /**
     * @return the character at the cursor position
     */
    public char peek() {
        return arguments.charAt(cursorPosition);
    }

    public char peekBackward() {
        if (cursorPosition == 0) {
            return (char) 0;
        }
        return arguments.charAt(cursorPosition-1);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Data reader methods
    ///////////////////////////////////////////////////////////////////////////

    public Integer readInteger() throws ArgumentSyntaxException {
        final String numberString = readUnquotedString();
        try {
            return Integer.parseInt(numberString);
        } catch (Exception e) {
            throw new ArgumentSyntaxException("Number format exception", numberString, 1001);
        }
    }

    public Double readDouble() throws ArgumentSyntaxException {
        final String numberString = readUnquotedString();
        try {
            return Double.parseDouble(numberString);
        } catch (Exception e) {
            throw new ArgumentSyntaxException("Number format exception", numberString, 1001);
        }
    }

    public Float readFloat() throws ArgumentSyntaxException {
        final String numberString = readUnquotedString();
        try {
            return Float.parseFloat(numberString);
        } catch (Exception e) {
            throw new ArgumentSyntaxException("Number format exception", numberString, 1001);
        }
    }

    public Boolean readBoolean() throws ArgumentSyntaxException {
        final String bool = readUnquotedString();
        try {
            return Boolean.parseBoolean(bool);
        } catch (Exception e) {
            throw new ArgumentSyntaxException("Invalid boolean", bool, 1002);
        }
    }

    public String readRemaining() {
        return arguments.substring(cursorPosition, (cursorPosition = arguments.length()));
    }

    public String readUnquotedString() {
        return readUntil(CharCondition.SPACE);
    }

    public String readQuotedString() throws ArgumentSyntaxException {
        final char quoteType = expect(CharCondition.QUOTED_STRING_START);
        return readUntil(quoteType == SINGLE_QUOTE ? SINGLE_QUOTED_STRING_END : DOUBLE_QUOTED_STRING_END);
    }

    public String readUnquotedJson() {
        throw new UnsupportedOperationException("Not implemented");
    }

    ///////////////////////////////////////////////////////////////////////////
    // Reader utils
    ///////////////////////////////////////////////////////////////////////////

    public String readUntil(CharCondition charCondition) {
        final int start = cursorPosition;
        while (canRead()) {
            if (charCondition.check(read())) {
                break;
            }
        }
        return arguments.substring(start, cursorPosition);
    }

    public char expect(CharCondition charCondition) throws ArgumentSyntaxException {
        final char c = read();
        if (charCondition.check(c)) {
            return c;
        } else {
            throw new ArgumentSyntaxException("Unexpected character", "" + c, 1003);
        }
    }

    interface CharCondition {
        CharCondition SPACE = value -> value == ' ';
        CharCondition QUOTED_STRING_START = value -> value == SINGLE_QUOTE || value == DOUBLE_QUOTES;

        boolean check(char value);
    }
}
