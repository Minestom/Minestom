package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.utils.math.FloatRange;

import java.util.regex.Pattern;

public class ArgumentFloatRange extends Argument<FloatRange> {

    public static final int FORMAT_ERROR = -1;

    public ArgumentFloatRange(String id) {
        super(id);
    }

    @Override
    public int getCorrectionResult(String value) {
        try {
            Float.valueOf(value);
            return SUCCESS; // Is a single number
        } catch (NumberFormatException e) {
            String[] split = value.split(Pattern.quote(".."));
            if (split.length == 2) {
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

    @Override
    public FloatRange parse(String value) {
        if (value.contains("..")) {
            String[] split = value.split(Pattern.quote(".."));
            final float min = Float.valueOf(split[0]);
            final float max = Float.valueOf(split[1]);
            return new FloatRange(min, max);
        } else {
            final float number = Float.valueOf(value);
            return new FloatRange(number, number);
        }
    }

    @Override
    public int getConditionResult(FloatRange value) {
        return SUCCESS;
    }
}
