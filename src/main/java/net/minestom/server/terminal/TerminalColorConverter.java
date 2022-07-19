package net.minestom.server.terminal;

/**
 * A string converter to convert a string to an ansi-colored one.
 *
 * Part of the code took from <a href="https://github.com/Minecrell/TerminalConsoleAppender/blob/master/src/main/java/net/minecrell/terminalconsole/MinecraftFormattingConverter.java">TerminalConsoleAppender</a>
 */
public final class TerminalColorConverter {
    public static final String ANSI_RESET = "\u001B[m";

    public static final char COLOR_CHAR = '§';
    public static final String LOOKUP = "0123456789abcdefklmnor";

    public static final String[] ANSI_CODES = new String[]{
            "\u001B[0;30m", // Black §0
            "\u001B[0;34m", // Dark Blue §1
            "\u001B[0;32m", // Dark Green §2
            "\u001B[0;36m", // Dark Aqua §3
            "\u001B[0;31m", // Dark Red §4
            "\u001B[0;35m", // Dark Purple §5
            "\u001B[0;33m", // Gold §6
            "\u001B[0;37m", // Gray §7
            "\u001B[0;30;1m",  // Dark Gray §8
            "\u001B[0;34;1m",  // Blue §9
            "\u001B[0;32;1m",  // Green §a
            "\u001B[0;36;1m",  // Aqua §b
            "\u001B[0;31;1m",  // Red §c
            "\u001B[0;35;1m",  // Light Purple §d
            "\u001B[0;33;1m",  // Yellow §e
            "\u001B[0;37;1m",  // White §f
            "\u001B[5m",       // Obfuscated §k
            "\u001B[21m",      // Bold §l
            "\u001B[9m",       // Strikethrough §m
            "\u001B[4m",       // Underline §n
            "\u001B[3m",       // Italic §o
            ANSI_RESET,        // Reset §r
    };

    /**
     * Format the given string
     *
     * @param string      the string to format
     * @param stripColors if true, colors will be stripped
     * @return the formatted string
     */
    public static String format(String string, boolean stripColors) {
        StringBuilder builder = new StringBuilder();
        int pos = 0;
        int size = string.length();
        while (pos < size) {
            char c = string.charAt(pos);
            if (c != COLOR_CHAR) {
                builder.append(c);
                pos++;
                continue;
            }
            if (pos + 1 >= size) {
                builder.append(COLOR_CHAR);
                pos++;
                continue;
            }
            char next = string.charAt(pos + 1);
            int format = LOOKUP.indexOf(Character.toLowerCase(next));
            if (format != -1) {
                if (!stripColors) {
                    builder.append(ANSI_CODES[format]);
                }
                pos += 2;
            } else {
                builder.append(c);
                pos++;
            }
        }
        if (!stripColors) {
            builder.append(ANSI_RESET);
        }
        return builder.toString();
    }
}
