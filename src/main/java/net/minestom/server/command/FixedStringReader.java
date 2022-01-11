package net.minestom.server.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.command.builder.exception.CommandException;
import org.jetbrains.annotations.NotNull;

/**
 * A class that provides access to a string by allowing code to "read" from it. This specific implementation is fixed,
 * so you cannot modify the string or the position of the cursor in the string. However, you can still read information
 * from it. If you want a mutable string reader implementation, see {@link StringReader}.<br>
 * Note that classes that implement this may make it mutable, but this implementation is still fixed. You can treat it as a
 * view of a mutable string reader.
 */
public sealed interface FixedStringReader permits StringReader {

    /**
     * @return the entire string that this reader is reading
     */
    @NotNull String all();

    /**
     * @return the position of this reader. This is equivalent to the number of characters read
     */
    int position();

    /**
     * @return the total number of characters in this reader
     */
    default int length() {
        return all().length();
    }

    /**
     * @return the number of remaining characters in this reader
     */
    default int remaining() {
        return all().length() - position();
    }

    /**
     * @return the portion of this reader that haven't been read yet
     */
    default @NotNull String unread() {
        return all().substring(position());
    }

    /**
     * @return the portion of this reader that has already been read
     */
    default @NotNull String previouslyRead() {
        return all().substring(0, position());
    }

    /**
     * @return true if at least {@code characters} can be read from this reader
     */
    default boolean canRead(int characters) {
        return position() + characters <= all().length();
    }

    /**
     * @return true if at least one character can be read from this reader
     */
    default boolean canRead() {
        return position() + 1 <= all().length();
    }

    /**
     * @return true if the provided string is equal to the next readable characters in this reader
     */
    default boolean canRead(@NotNull String text) {
        if (text.length() == 0) {
            return true;
        }
        if (text.length() == 1) {
            return canRead(1) && peek() == text.codePointAt(0);
        }
        return canRead(text.length()) && all().regionMatches(position(), text, 0, text.length());
    }

    /**
     * @return true if the provided string is equal to the next readable characters in this reader, ignoring case if
     * {@code ignoreCase} is true
     */
    default boolean canRead(@NotNull String text, boolean ignoreCase) {
        if (!ignoreCase) {
            return canRead(text);
        }
        if (text.length() == 0) {
            return true;
        }
        return canRead(text.length()) && all().regionMatches(true, position(), text, 0, text.length());
    }

    /**
     * @return the next readable character, as a Unicode code point. This does not move the position
     */
    default int peek() {
        return all().codePointAt(position());
    }

    /**
     * @return the next readable character, as a {@code char}. This does not move the position
     */
    default char peekChar() {
        return all().charAt(position());
    }

    /**
     * @return the character that is {@code offset} characters ahead of the position, as a Unicode code point. This does
     *         not move the position
     */
    default int peek(int offset) {
        return all().codePointAt(position() + offset);
    }

    /**
     * @return the character that is {@code offset} characters ahead of the position, as a {@code char}. This does not
     *         move the position
     */
    default char peekChar(int offset) {
        return all().charAt(position() + offset);
    }

    /**
     * @see #generateContextMessage()
     */
    default @NotNull Component generateContextMessage() {
        return generateContextMessage(all(), position());
    }

    /**
     * Generates a context message for this reader. The previously read text is properly formatted and cut off, and
     * the unread text is formatted. Here's an example of the output (using MiniMessage syntax) with
     * the translatable components converted to the en-US locale:<br>
     * Input: "/gamemode survival creative"<br>
     * Position: 19<br>
     * Output: {@code &lt;gray&gt;... survival &lt;/gray&gt;&lt;red&gt;&lt;underlined&gt;creative&lt;/underlined&gt;&lt;/red&gt;&lt;red&gt;&lt;italic&gt;&lt;--HERE&lt;/italic&gt;&lt;/red&gt;"}<br>
     */
    static @NotNull Component generateContextMessage(@NotNull String all, int position) {
        String prev = (position > CommandException.CUTOFF_LENGTH) ?
                ("..." + all.substring(position - CommandException.CUTOFF_LENGTH, position)) :
                all.substring(0, position);

        Component read = Component.text(prev, NamedTextColor.GRAY);
        Component error = Component.text(all.substring(position), NamedTextColor.RED, TextDecoration.UNDERLINED);

        return Component.text().append(read, error, CommandException.CONTEXT_HERE).build();
    }

    default @NotNull String asString() {
        return getClass().getSimpleName() + "[read=\"" + previouslyRead() + "\", unread=\"" + unread() +
                "\", position=" + position() + "]";
    }
}