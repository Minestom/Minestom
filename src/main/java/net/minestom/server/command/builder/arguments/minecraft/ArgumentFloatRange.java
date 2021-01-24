package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.utils.math.FloatRange;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * Represents an argument which will give you an {@link FloatRange}.
 * <p>
 * Example: ..3, 3.., 5..10, 15
 */
public class ArgumentFloatRange extends ArgumentRange<FloatRange> {

    public ArgumentFloatRange(String id) {
        super(id);
    }

    @NotNull
    @Override
    public FloatRange parse(@NotNull String input) throws ArgumentSyntaxException {
        try {
            if (input.contains("..")) {
                final int index = input.indexOf('.');
                final String[] split = input.split(Pattern.quote(".."));

                final float min;
                final float max;
                if (index == 0) {
                    // Format ..NUMBER
                    min = Float.MIN_VALUE;
                    max = Float.parseFloat(split[0]);
                } else {
                    if (split.length == 2) {
                        // Format NUMBER..NUMBER
                        min = Float.parseFloat(split[0]);
                        max = Float.parseFloat(split[1]);
                    } else {
                        // Format NUMBER..
                        min = Float.parseFloat(split[0]);
                        max = Float.MAX_VALUE;
                    }
                }

                return new FloatRange(min, max);
            } else {
                final float number = Float.parseFloat(input);
                return new FloatRange(number);
            }
        } catch (NumberFormatException e2) {
            throw new ArgumentSyntaxException("Invalid number", input, FORMAT_ERROR);
        }
    }
}
