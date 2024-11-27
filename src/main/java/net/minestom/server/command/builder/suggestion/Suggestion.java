package net.minestom.server.command.builder.suggestion;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A class representing the tab completion of a command. Created by the command parser, and passed to user-defined
 * {@link SuggestionCallback} instances when tab completion is requested by a client.
 * Use {@link Suggestion#addEntry(SuggestionEntry)} to add a possible completion value.
 */
public class Suggestion {

    private final String input;
    private int start;
    private int length;
    private final List<SuggestionEntry> suggestionEntries = new ArrayList<>();

    /**
     * Creates a new Suggestion; used by the command parser.
     *
     * @param input the input string (command that was typed so far)
     * @param start the initial starting index of the argument to be completed
     * @param length the initial length of the argument to be completed
     */
    @ApiStatus.Internal
    public Suggestion(@NotNull String input, int start, int length) {
        validateSubstring(start, length, input.length());

        this.input = input;
        this.start = start;
        this.length = length;
    }

    private static void validateSubstring(int start, int length, int totalLength) {
        if (start < 0)
            throw new IllegalArgumentException("negative start index " + start);

        if (length < 0)
            throw new IllegalArgumentException("negative length " + length);

        if (start + length > totalLength)
            throw new IllegalArgumentException("range [" + start + ", " + (start + length) +
                    ") out of bounds for length " + totalLength);
    }

    /**
     * Returns the input string. This is the entire command that has been typed so far, excluding the leading {@code /}.
     *
     * @return the command string
     */
    @NotNull
    public String getInput() {
        return input;
    }

    /**
     * Returns the starting index of the argument that is being tab-completed.
     *
     * @return the starting index of the argument
     */
    public int getStart() {
        return start;
    }

    /**
     * Sets the starting index of the argument being completed.
     *
     * @param start the starting index
     * @throws IllegalArgumentException if {@code start < 0}, or {@code start + getLength()} &gt; {@code getInput().length()}
     */
    public void setStart(int start) {
        validateSubstring(start, length, input.length());
        this.start = start;
    }

    /**
     * Gets the length of the argument being completed.
     *
     * @return the length of the argument being completed
     */
    public int getLength() {
        return length;
    }

    /**
     * Sets the length of the argument being completed.
     *
     * @param length the argument length
     * @throws IllegalArgumentException if {@code length < 0}, or {@code getStart() + length} &gt; {@code getInput().length()}
     */
    public void setLength(int length) {
        validateSubstring(start, length, input.length());
        this.length = length;
    }

    /**
     * Gets the current substring of the input ({@link Suggestion#getInput()}) that is eligible for completion.
     *
     * @return a substring of {@code getInput()}
     */
    @NotNull
    public String getCurrent() {
        return input.substring(start, start + length);
    }

    /**
     * Gets the mutable list of {@link SuggestionEntry}s that represent possible tab completion values.
     *
     * @return a mutable list of SuggestionEntry instances
     */
    @NotNull
    public List<SuggestionEntry> getEntries() {
        return suggestionEntries;
    }

    /**
     * Adds a new {@link SuggestionEntry}, representing a possible tab completion value.
     *
     * @param entry the new entry
     */
    public void addEntry(@NotNull SuggestionEntry entry) {
        this.suggestionEntries.add(entry);
    }

}
