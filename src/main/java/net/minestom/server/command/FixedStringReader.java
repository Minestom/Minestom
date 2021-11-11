package net.minestom.server.command;

import org.jetbrains.annotations.NotNull;

/**
 * A class that provides access to a string by allowing code to "read" from it. This specific implementation is fixed,
 * so you cannot modify the string or the position of the cursor in the string. However, you can still read information
 * from it. If you want a mutable string reader implementation, see {@link StringReader}.<br>
 * Note that classes that extend this may make it mutable, but this implementation is still fixed. You can treat it as a
 * view of a mutable string reader.
 */
public sealed class FixedStringReader permits StringReader {

    private final @NotNull String input;
    /**
     * This is the current position of the string reader.
     */
    protected int currentPosition;

    /**
     * Creates a fixed string reader that will read from the following input, starting at the start of the string.
     */
    public FixedStringReader(@NotNull String input) {
        this(input, 0);
    }

    /**
     * Creates a fixed string reader that will read from the following input, starting at the provided position.
     */
    public FixedStringReader(@NotNull String input, int startingPosition) {
        this.input = input;
        this.currentPosition = startingPosition;
    }

    /**
     * @return the string that is being read from
     */
    public @NotNull String all() {
        return input;
    }

    /**
     * @return the current position in the string that is getting read
     */
    public int currentPosition() {
        return currentPosition;
    }

    /**
     * @return the number of remaining characters in the string
     */
    public int remainingCharacters() {
        return input.length() - currentPosition;
    }

    /**
     * @return the total length of the string
     */
    public int length() {
        return input.length();
    }

    /**
     * @return all the characters that have been previously read
     */
    public @NotNull String previouslyRead() {
        return input.substring(0, currentPosition);
    }

    /**
     * @return all characters that have not been read yet
     */
    public @NotNull String unreadCharacters() {
        return input.substring(currentPosition);
    }

    /**
     * @return true if the string has at least {@code characters} more readable characters, otherwise false
     */
    public boolean canRead(int characters) {
        return currentPosition + characters <= input.length();
    }

    /**
     * @return true if the string has at least one more readable character, otherwise false
     */
    public boolean canRead() {
        return canRead(1);
    }

    /**
     * @return the next readable character, without moving the cursor forwards
     */
    public char peek() {
        return input.charAt(currentPosition);
    }

    /**
     * @return the character that is {@code offset} characters ahead of the cursor, without moving the cursor forwards
     */
    public char peek(int offset) {
        return input.charAt(currentPosition + offset);
    }

}