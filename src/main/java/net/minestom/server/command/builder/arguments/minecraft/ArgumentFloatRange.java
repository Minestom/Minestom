package net.minestom.server.command.builder.arguments.minecraft;

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

    @Override
    public int getCorrectionResult(@NotNull String value) {
        try {
            Float.valueOf(value);
            return SUCCESS; // Is a single number
        } catch (NumberFormatException e) {
            String[] split = value.split(Pattern.quote(".."));
            if (split.length == 1) {
                try {
                    Float.valueOf(split[0]);
                    return SUCCESS;
                } catch (NumberFormatException e2) {
                    return FORMAT_ERROR;
                }
            } else if (split.length == 2) {
                try {
                    Float.valueOf(split[0]); // min
                    Float.valueOf(split[1]); // max
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
    public FloatRange parse(@NotNull String value) {
        if (value.contains("..")) {
            final int index = value.indexOf('.');
            final String[] split = value.split(Pattern.quote(".."));

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
            final float number = Float.parseFloat(value);
            return new FloatRange(number);
        }
    }
}
