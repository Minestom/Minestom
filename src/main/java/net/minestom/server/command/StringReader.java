package net.minestom.server.command;

import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * A mutable string reader implementation.
 */
public final class StringReader implements FixedStringReader {

    private static final char DOUBLE_QUOTE = '"', SINGLE_QUOTE = '\'', ESCAPE = '\\';

    /**
     * @return true if the character is a valid letter
     */
    public static boolean isValidLetter(int c) {
        c = Character.toLowerCase(c);
        return (c >= 'a' && c <= 'z');
    }

    /**
     * @return true if the character is a number (0 to 9), a period ('.'), or a dash ('-')
     */
    public static boolean isValidNumber(int c) {
        return (c >= '0' && c <= '9') || (c == '.') || (c == '-');
    }

    /**
     * @return true if the character is an apostrophe or quotation marks
     */
    public static boolean isValidQuote(int c) {
        return c == DOUBLE_QUOTE || c == SINGLE_QUOTE;
    }

    /**
     * @return true if the character is an underscore ('_'), a plus ('+'), a valid number according to {@link
     * #isValidNumber(int)}, or a valid letter according to {@link #isValidLetter(int)
     */
    public static boolean isValidUnquotedCharacter(int c) {
        return c == '_' || c == '+' || isValidNumber(c) || isValidLetter(c);
    }

    /**
     * @return true if the character is a valid whitespace character
     */
    public static boolean isValidWhitespace(int c) {
        return Character.isWhitespace(c);
    }

    private final @NotNull String all;
    private int position;

    /**
     * Creates a string reader that will read from the following input, starting at the start of the string.
     */
    public StringReader(@NotNull String all) {
        this(all, 0);
    }

    /**
     * Creates a string reader that will read from the following input, starting at the provided position.
     */
    public StringReader(@NotNull String all, int startingPosition) {
        this.all = all;
        this.position = startingPosition;
    }

    /**
     * @return the string that is being read from
     */
    @Override
    public @NotNull String all() {
        return all;
    }

    /**
     * @return the current position in the string that is getting read
     */
    @Override
    public int position() {
        return position;
    }

    /**
     * Skips the next character/code point without reading it.
     */
    public void skip() {
        position++;
    }

    /**
     * Skips the provided number of characters
     */
    public void skip(int chars) {
        position += chars;
    }

    /**
     * @return the next readable character, as a code point, incrementing the current position in the process
     */
    public int next() {
        return all().codePointAt(position++);
    }

    /**
     * @return the next readable character, incrementing the current position in the process
     */
    public char nextChar() {
        return all().charAt(position++);
    }

    /**
     * Sets the current position to the provided new position. This is not checked, so errors will likely occur if the
     * provided value is below zero or greater than or equal to than the length of the string.
     */
    public void position(int newPosition) {
        this.position = newPosition;
    }

    /**
     * Keeps reading characters until there are no characters left or the next character is not a valid unquoted
     * character, then returns all characters that were read.
     * @return the complete, unquoted string
     */
    public @NotNull String readUnquotedString() {
        final int start = position;
        while (canRead() && isValidUnquotedCharacter(peek())) {
            skip();
        }
        return all().substring(start, position);
    }

    /**
     * @return the unread portion of the input string, moving the cursor forwards as it gets read
     */
    public @NotNull String readAll() {
        String remaining = unread();
        position = all().length();
        return remaining;
    }

    /**
     * Skips characters until the end of the string has been reached or the next character is whitespace according to
     * {@link StringReader#isValidWhitespace(int)}.
     */
    public void skipWhitespace() {
        while (canRead() && isValidWhitespace(peek())) {
            skip();
        }
    }

    /**
     * If there is any directly readable whitespace, this skips it. If there is not, an exception is thrown. This is
     * similar to {@link #assureWhitespaceCharacter()} except that this method reads all available whitespace instead of
     * just the next character.
     */
    public void assureWhitespace() throws CommandException {
        int start = position;
        skipWhitespace();
        if (start == position) {
            throw CommandException.COMMAND_EXPECTED_SEPARATOR.generateException(this);
        }
    }

    /**
     * If the next character is a whitespace character according to {@link StringReader#isValidWhitespace(int)}, it
     * gets skipped. If it is not, an exception is thrown.
     */
    public void assureWhitespaceCharacter() throws CommandException {
        if (!canRead() || !isValidWhitespace(peek())) {
            throw CommandException.COMMAND_EXPECTED_SEPARATOR.generateException(this);
        }
        skip();
    }

    /**
     * @return the next integer in the string
     */
    public int readInteger() throws CommandException {
        int start = position;
        while (canRead() && isValidNumber(peek())) {
            skip();
        }
        if (position == start) {
            throw CommandException.PARSING_INT_EXPECTED.generateException(this);
        }
        String number = all().substring(start, position);
        if (number.isBlank()) {
            throw CommandException.PARSING_INT_EXPECTED.generateException(this);
        }
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException exception) {
            position = start;
            throw CommandException.PARSING_INT_INVALID.generateException(this, number);
        }
    }

    /**
     * @return the next long in the string
     */
    public long readLong() throws CommandException {
        int start = position;
        while (canRead() && isValidNumber(peek())) {
            skip();
        }
        if (position == start) {
            throw CommandException.PARSING_LONG_EXPECTED.generateException(this);
        }
        String number = all().substring(start, position);
        if (number.isBlank()) {
            throw CommandException.PARSING_LONG_EXPECTED.generateException(this);
        }
        try {
            return Long.parseLong(number);
        } catch (NumberFormatException exception) {
            position = start;
            throw CommandException.PARSING_LONG_INVALID.generateException(this, number);
        }
    }

    /**
     * @return the next double in the string
     */
    public double readDouble() throws CommandException {
        int start = position;
        while (canRead() && isValidNumber(peek())) {
            skip();
        }
        if (position == start) {
            throw CommandException.PARSING_DOUBLE_EXPECTED.generateException(this);
        }
        String number = all().substring(start, position);
        if (number.isBlank()) {
            throw CommandException.PARSING_DOUBLE_EXPECTED.generateException(this);
        }
        try {
            return Double.parseDouble(number);
        } catch (NumberFormatException exception) {
            position = start;
            throw CommandException.PARSING_DOUBLE_INVALID.generateException(this, number);
        }
    }

    /**
     * @return the next float in the string
     */
    public float readFloat() throws CommandException {
        int start = position;
        while (canRead() && isValidNumber(peek())) {
            skip();
        }
        if (position == start) {
            throw CommandException.PARSING_FLOAT_EXPECTED.generateException(this);
        }
        String number = all().substring(start, position);
        if (number.isBlank()) {
            throw CommandException.PARSING_FLOAT_EXPECTED.generateException(this);
        }
        try {
            return Float.parseFloat(number);
        } catch (NumberFormatException exception) {
            position = start;
            throw CommandException.PARSING_FLOAT_INVALID.generateException(this, number);
        }
    }

    /**
     * @return the next boolean in the string
     */
    public boolean readBoolean() throws CommandException {
        int start = position;
        String next = readString();
        if (next.isEmpty() || next.isBlank()) {
            throw CommandException.PARSING_BOOL_EXPECTED.generateException(this);
        } else if (next.equals("true")) {
            return true;
        } else if (next.equals("false")) {
            return false;
        } else {
            position = start;
            throw CommandException.PARSING_BOOL_INVALID.generateException(this, next);
        }
    }

    /**
     * @return the rest of the string or until there is a character equal to the {@code terminatorCodePoint} parameter
     */
    public @NotNull String readStringUntil(int terminatorCodePoint) throws CommandException {
        final StringBuilder result = new StringBuilder();
        boolean escaped = false;
        while (canRead()) {
            final int c = next();
            if (escaped) {
                if (c == terminatorCodePoint || c == ESCAPE) {
                    result.append(c);
                    escaped = false;
                } else {
                    position--;
                    throw CommandException.PARSING_QUOTE_ESCAPE.generateException(this, String.valueOf(c));
                }
            } else if (c == ESCAPE) {
                escaped = true;
            } else if (c == terminatorCodePoint) {
                return result.toString();
            } else {
                result.append(c);
            }
        }
        throw CommandException.PARSING_QUOTE_EXPECTED_END.generateException(this);
    }

    /**
     * @return the next quoted or unquoted string in the input
     */
    public @NotNull String readString() throws CommandException {
        if (!canRead()) {
            return "";
        }
        final int next = peek();
        if (isValidQuote(next)) {
            skip();
            return readStringUntil(next);
        }
        return readUnquotedString();
    }

    /**
     * @return the next NamespaceID in the input
     */
    public @NotNull NamespaceID readNamespaceID() throws CommandException {
        String next = readUnquotedString();
        try {
            return NamespaceID.from(next);
        } catch (AssertionError error) {
            throw CommandException.ARGUMENT_ID_INVALID.generateException(this);
        }
    }

    /**
     * @return the next string surrounded by quotes (the quotes must be the same character, e.g. the string cannot start
     * with ' and end with ").
     */
    public @NotNull String readQuotedString() throws CommandException {
        if (!canRead()) {
            return "";
        }
        final int next = peek();
        if (!isValidQuote(next)) {
            throw CommandException.PARSING_QUOTE_EXPECTED_START.generateException(this);
        }
        skip();
        return readStringUntil(next);
    }

    /**
     * @return the next UUID in the string
     */
    public @NotNull UUID readUUID() throws CommandException {
        int start = position;
        while (canRead()) {
            int c = peek();
            if (!(c == '-' || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f') || (c >= '0' && c <= '9'))) {
                break;
            }
            skip();
        }
        try {
            return UUID.fromString(all().substring(start, position));
        } catch (IllegalArgumentException exception) {
            throw CommandException.ARGUMENT_UUID_INVALID.generateException(this);
        }
    }

    /**
     * Throws an exception if the next character is not {@code codePoint}, and then skips the next character.
     */
    public void expect(int codePoint) throws CommandException {
        if (!canRead() || codePoint != peek()) {
            throw CommandException.PARSING_EXPECTED.generateException(this, Character.toString(codePoint));
        }
        skip();
    }

    @Override
    public @NotNull String toString() {
        return asString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StringReader that = (StringReader) o;
        return position == that.position && all.equals(that.all);
    }

    @Override
    public int hashCode() {
        return all.hashCode() * 31 + position * 7;
    }
}