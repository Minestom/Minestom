package net.minestom.server.command.builder.arguments;

import org.jetbrains.annotations.NotNull;

/**
 * Argument which will take a quoted string.
 * <p>
 * Example: "Hey I am a string"
 */
public class ArgumentString extends Argument<String> {

    public static final int QUOTE_ERROR = 1;

    public ArgumentString(String id) {
        super(id, true);
    }

    @Override
    public int getCorrectionResult(@NotNull String value) {
        // Check if value start and end with quote
        final char first = value.charAt(0);
        final char last = value.charAt(value.length() - 1);
        final boolean quote = first == '\"' && last == '\"';
        if (!quote)
            return QUOTE_ERROR;

        for (int i = 1; i < value.length(); i++) {
            final char c = value.charAt(i);
            if (c == '\"') {
                final char lastChar = value.charAt(i - 1);
                if (lastChar == '\\') {
                    continue;
                } else if (i == value.length() - 1) {
                    return SUCCESS;
                }
            }
        }

        // Last quote is written like \"
        return QUOTE_ERROR;
    }

    @NotNull
    @Override
    public String parse(@NotNull String value) {
        // Remove first and last characters (quote)
        value = value.substring(1, value.length() - 1);

        // Remove all backslashes
        value = value.replace("\\", "");

        return value;
    }

    @Override
    public int getConditionResult(@NotNull String value) {
        return SUCCESS;
    }
}
