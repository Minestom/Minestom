package net.minestom.server.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;

/**
 * A class that provides access to a string by allowing code to "read" from it. This specific implementation is fixed,
 * so you cannot modify the string or the position of the cursor in the string. However, you can still read information
 * from it. If you want a mutable string reader implementation, see {@link StringReader}.<br>
 * Note that classes that extend this may make it mutable, but this implementation is still fixed. You can treat it as a
 * view of a mutable string reader.
 */
public sealed class FixedStringReader permits StringReader {

    /**
     * A static style instance that represents {@link NamedTextColor#RED}. This is here so that some style instances
     * don't have to be created each time they're used.
     */
    public static final @NotNull Style RED_STYLE = Style.style(NamedTextColor.RED);

    /**
     * A static style instance that represents {@link NamedTextColor#RED} and {@link TextDecoration#UNDERLINED}. This is
     * here so that some style instances don't have to be created each time they're used.
     */
    public static final @NotNull Style RED_UNDERLINED_STYLE = Style.style(NamedTextColor.RED, TextDecoration.UNDERLINED);

    /**
     * A static style instance that represents {@link NamedTextColor#GRAY}. This is here so that gray style instances
     * don't have to be created each time they're used.
     */
    public static final @NotNull Style GRAY_STYLE = Style.style(NamedTextColor.GRAY);

    /**
     * A static translatable component that represents the message that appears after incorrect command syntax, e.g.
     * "/gamemode test<--[HERE]". It's automatically styled to be red and italicised.
     */
    public static final @NotNull Component CONTEXT_HERE = Component.translatable("command.context.here", NamedTextColor.RED, TextDecoration.ITALIC);

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

    /**
     * Generates a context message for this instance. Here's an example of the output (using MiniMessage syntax) with
     * the translatable components converted to the en-US locale:<br>
     * Input: "/gamemode survival creative"<br>
     * Position: 19<br>
     * Output: "&lt;gray&gt; survival &lt;/gray&gt;&lt;red&gt;&lt;underlined&gt;survival&lt;/underlined&gt;&lt;/red&gt;
     * &lt;red&gt;&lt;italic&gt;&lt;--HERE&lt;/italic&gt;&lt;/red&gt;"<br>
     */
    public @NotNull Component generateContextMessage(){
        Component read = Component.text(this.input.substring(Math.max(this.currentPosition - 10, 0), this.currentPosition), GRAY_STYLE);
        Component error = Component.text(this.unreadCharacters(), RED_UNDERLINED_STYLE);

        return Component.text().append(read, error, CONTEXT_HERE).build();
    }

}