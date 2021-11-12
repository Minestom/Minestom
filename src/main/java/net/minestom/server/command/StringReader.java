package net.minestom.server.command;

import net.minestom.server.command.builder.exception.SectionParsingException;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.command.builder.exception.SectionParsingException.*;

/**
 * A mutable string reader implementation.
 */
public final class StringReader extends FixedStringReader {

    private static final char DOUBLE_QUOTE = '"', SINGLE_QUOTE = '\'', ESCAPE = '\\';

    /**
     * @return true if the character is a valid letter
     */
    public static boolean isValidLetter(int c){
        c = Character.toLowerCase(c);
        return (c >= 'a' && c <= 'z');
    }

    /**
     * @return true if the character is a number (0 to 9), a period ('.'), or a dash ('-')
     */
    public static boolean isValidNumber(int c){
        return (c >= '0' && c <= '9') || (c == '.') || (c == '-');
    }

    /**
     * @return true if the character is an apostrophe or quotation marks
     */
    public static boolean isValidQuote(int c){
        return c == DOUBLE_QUOTE || c == SINGLE_QUOTE;
    }

    /**
     * @return true if the character is an underscore ('_'), a plus ('+'), a valid number according to {@link
     * #isValidNumber(int)}, or a valid letter according to {@link #isValidLetter(int)
     */
    public static boolean isValidUnquotedCharacter(int c){
        return c == '_' || c == '+' || isValidNumber(c) || isValidLetter(c);
    }

    /**
     * Creates a string reader that will read from the following input, starting at the start of the string.
     */
    public StringReader(@NotNull String input) {
        super(input);
    }

    /**
     * Creates a string reader that will read from the following input, starting at the provided position.
     */
    public StringReader(@NotNull String input, int startingPosition) {
        super(input, startingPosition);
    }

    /**
     * Skips the next character without reading it.
     */
    public void skip() {
        currentPosition++;
    }

    /**
     * @return the next readable character, incrementing the current position in the process
     */
    public char nextChar() {
        currentPosition++;
        return all().charAt(currentPosition);
    }

    /**
     * Sets the current position to the provided new position. This is not checked, so errors will likely occur if the
     * provided value is below zero or greater than or equal to than the length of the string.
     */
    public void currentPosition(int newPosition){
        this.currentPosition = newPosition;
    }

    /**
     * Keeps reading characters until there are no characters left or the next character is not a valid unquoted
     * character, then returns all characters that were read.
     * @return the complete, unquoted string
     */
    public @NotNull String readUnquotedString() {
        final int start = currentPosition;
        while (canRead() && isValidUnquotedCharacter(peek())) {
            skip();
        }
        return all().substring(start, currentPosition);
    }

    /**
     * @return the unread portion of the input string, moving the cursor forwards as it gets read
     */
    public @NotNull String readAll() {
        String remaining = unreadCharacters();
        currentPosition = all().length();
        return remaining;
    }

    /**
     * Skips characters until the end of the string has been reached or the next character is whitespace according to
     * {@link Character#isWhitespace(char)}.
     */
    public void skipWhitespace() {
        while (canRead() && Character.isWhitespace(peek())) {
            skip();
        }
    }

    /**
     * @return the next integer in the string
     */
    public int readInteger() throws SectionParsingException {
        int start = currentPosition;
        while(canRead() && isValidNumber(peek())){
            skip();
        }
        if (currentPosition == start){
            throw new SectionParsingException(INT_EXPECTED, this);
        }
        String number = all().substring(start, currentPosition);
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException exception){
            currentPosition = start;
            throw new SectionParsingException(INT_INVALID, this, number);
        }
    }

    /**
     * @return the next long in the string
     */
    public long readLong() throws SectionParsingException {
        int start = currentPosition;
        while(canRead() && isValidNumber(peek())){
            skip();
        }
        if (currentPosition == start){
            throw new SectionParsingException(LONG_EXPECTED, this);
        }
        String number = all().substring(start, currentPosition);
        try {
            return Long.parseLong(number);
        } catch (NumberFormatException exception){
            currentPosition = start;
            throw new SectionParsingException(LONG_INVALID, this, number);
        }
    }

    /**
     * @return the next double in the string
     */
    public double readDouble() throws SectionParsingException {
        int start = currentPosition;
        while(canRead() && isValidNumber(peek())){
            skip();
        }
        if (currentPosition == start){
            throw new SectionParsingException(DOUBLE_EXPECTED, this);
        }
        String number = all().substring(start, currentPosition);
        try {
            return Double.parseDouble(number);
        } catch (NumberFormatException exception){
            currentPosition = start;
            throw new SectionParsingException(DOUBLE_INVALID, this, number);
        }
    }

    /**
     * @return the next float in the string
     */
    public float readFloat() throws SectionParsingException {
        int start = currentPosition;
        while(canRead() && isValidNumber(peek())){
            skip();
        }
        if (currentPosition == start){
            throw new SectionParsingException(FLOAT_EXPECTED, this);
        }
        String number = all().substring(start, currentPosition);
        try {
            return Float.parseFloat(number);
        } catch (NumberFormatException exception){
            currentPosition = start;
            throw new SectionParsingException(FLOAT_INVALID, this, number);
        }
    }

    /**
     * @return the rest of the string or until there is a character equal to the terminator parameter
     */
    public @NotNull String readStringUntil(char terminator) throws SectionParsingException {
        final StringBuilder result = new StringBuilder();
        boolean escaped = false;
        while (canRead()) {
            final char c = nextChar();
            if (escaped) {
                if (c == terminator || c == ESCAPE) {
                    result.append(c);
                    escaped = false;
                } else {
                    currentPosition--;
                    throw new SectionParsingException(QUOTE_ESCAPE, this, Character.toString(c));
                }
            } else if (c == ESCAPE) {
                escaped = true;
            } else if (c == terminator) {
                return result.toString();
            } else {
                result.append(c);
            }
        }
        throw new SectionParsingException(QUOTE_EXPECTED_END, this);
    }

    /**
     * @return the next quoted or unquoted string in the input
     */
    public @NotNull String readString() throws SectionParsingException {
        if (!canRead()) {
            return "";
        }
        final char next = peek();
        if (isValidQuote(next)) {
            skip();
            return readStringUntil(next);
        }
        return readUnquotedString();
    }

    /**
     * @return the next string surrounded by quotes (the quotes must be the same character, e.g. the string cannot start
     * with ' and end with ").
     */
    public @NotNull String readQuotedString() throws SectionParsingException {
        if (!canRead()) {
            return "";
        }
        final char next = peek();
        if (!isValidQuote(next)) {
            throw new SectionParsingException(QUOTE_EXPECTED_START, this);
        }
        skip();
        return readStringUntil(next);
    }

    /**
     * @return the next boolean in the string
     */
    public boolean readBoolean() throws SectionParsingException {
        int start = currentPosition;
        String next = readString();
        if (next.isEmpty()) {
            throw new SectionParsingException(BOOL_EXPECTED, this);
        } else if (next.equals("true")) {
            return true;
        } else if (next.equals("false")){
            return false;
        } else {
            currentPosition = start;
            throw new SectionParsingException(BOOL_INVALID, this, next);
        }
    }

    /**
     * Throws an exception if the next character is not {@code c}, and then skips the next character.
     */
    public void expect(char c) throws SectionParsingException {
        if (!canRead() || c != peek()) {
            throw new SectionParsingException(EXPECTED, this, Character.toString(c));
        }
        skip();
    }
}