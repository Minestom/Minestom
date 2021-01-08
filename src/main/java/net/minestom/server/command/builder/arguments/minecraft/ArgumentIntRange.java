package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.utils.math.IntRange;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * Represents an argument which will give you an {@link IntRange}.
 * <p>
 * Example: ..3, 3.., 5..10, 15
 */
public class ArgumentIntRange extends ArgumentRange<IntRange> {

    public ArgumentIntRange(String id) {
        super(id);
    }

    @NotNull
    @Override
    public IntRange parse(@NotNull String input) throws ArgumentSyntaxException {
        return staticParse(input);
    }

    @NotNull
    public static IntRange staticParse(@NotNull String input) throws ArgumentSyntaxException {
        try {
            if (input.contains("..")) {
                final int index = input.indexOf('.');
                final String[] split = input.split(Pattern.quote(".."));

                final int min;
                final int max;
                if (index == 0) {
                    // Format ..NUMBER
                    min = Integer.MIN_VALUE;
                    max = Integer.parseInt(split[0]);
                } else {
                    if (split.length == 2) {
                        // Format NUMBER..NUMBER
                        min = Integer.parseInt(split[0]);
                        max = Integer.parseInt(split[1]);
                    } else {
                        // Format NUMBER..
                        min = Integer.parseInt(split[0]);
                        max = Integer.MAX_VALUE;
                    }
                }

                return new IntRange(min, max);
            } else {
                final int number = Integer.parseInt(input);
                return new IntRange(number);
            }
        } catch (NumberFormatException e2) {
            throw new ArgumentSyntaxException("Invalid number", input, FORMAT_ERROR);
        }
    }
}
