package net.minestom.server.command;

import org.jetbrains.annotations.NotNull;

/**
 * A mutable string reader implementation.
 */
public final class StringReader extends FixedStringReader {

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
        return c == '"' || c == '\'';
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
     * Keeps reading characters until there are no characters left or the next character is not a valid unquoted
     * character, then returns all characters that were read.
     * @return the complete, unquoted string
     */
    public @NotNull String readUnquotedString() {
        final int start = currentPosition;
        while(canRead() && isValidUnquotedCharacter(peek())) {
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

}