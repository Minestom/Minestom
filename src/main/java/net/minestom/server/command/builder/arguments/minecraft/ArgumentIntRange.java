package net.minestom.server.command.builder.arguments.minecraft;

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

    @Override
    public int getCorrectionResult(@NotNull String value) {
        try {
            Integer.valueOf(value);
            return SUCCESS; // Is a single number
        } catch (NumberFormatException e) {
            String[] split = value.split(Pattern.quote(".."));
            if (split.length == 1) {
                try {
                    Integer.valueOf(split[0]);
                    return SUCCESS;
                } catch (NumberFormatException e2) {
                    return FORMAT_ERROR;
                }
            } else if (split.length == 2) {
                try {
                    Integer.valueOf(split[0]); // min
                    Integer.valueOf(split[1]); // max
                    return SUCCESS;
                } catch (NumberFormatException e2) {
                    return FORMAT_ERROR;
                }
            } else {
                return FORMAT_ERROR;
            }
        }
    }

    @NotNull
    @Override
    public IntRange parse(@NotNull String value) {
        if (value.contains("..")) {
            final int index = value.indexOf('.');
            final String[] split = value.split(Pattern.quote(".."));

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
            final int number = Integer.parseInt(value);
            return new IntRange(number);
        }
    }
}
