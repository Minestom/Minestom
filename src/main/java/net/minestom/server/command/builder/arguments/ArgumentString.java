package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
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

    @NotNull
    @Override
    public String parse(@NotNull String input) throws ArgumentSyntaxException {
        // Check if value start and end with quote
        final char first = input.charAt(0);
        final char last = input.charAt(input.length() - 1);
        final boolean quote = first == '\"' && last == '\"';
        if (!quote)
            throw new ArgumentSyntaxException("String argument needs to start and end with quotes", input, QUOTE_ERROR);

        // Verify backslashes
        for (int i = 1; i < input.length(); i++) {
            final char c = input.charAt(i);
            if (c == '\"') {
                final char lastChar = input.charAt(i - 1);
                if (lastChar == '\\') {
                    continue;
                } else if (i == input.length() - 1) {

                    // Remove first and last characters (quote)
                    input = input.substring(1, input.length() - 1);

                    // Remove all backslashes
                    input = input.replace("\\", "");

                    return input;
                }
            }
        }

        throw new ArgumentSyntaxException("Last quote is escaped", input, QUOTE_ERROR);
    }
}
