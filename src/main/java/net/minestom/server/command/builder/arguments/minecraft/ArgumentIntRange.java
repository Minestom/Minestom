package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.utils.math.IntRange;

import java.util.regex.Pattern;

public class ArgumentIntRange extends Argument<IntRange> {

    public static final int FORMAT_ERROR = -1;

    public ArgumentIntRange(String id) {
        super(id);
    }

    @Override
    public int getCorrectionResult(String value) {
        try {
            Integer.valueOf(value);
            return SUCCESS; // Is a single number
        } catch (NumberFormatException e) {
            String[] split = value.split(Pattern.quote(".."));
            if (split.length == 2) {
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

    @Override
    public IntRange parse(String value) {
        if (value.contains("..")) {
            String[] split = value.split(Pattern.quote(".."));
            final int min = Integer.valueOf(split[0]);
            final int max = Integer.valueOf(split[1]);
            return new IntRange(min, max);
        } else {
            final int number = Integer.valueOf(value);
            return new IntRange(number, number);
        }
    }

    @Override
    public int getConditionResult(IntRange value) {
        return SUCCESS;
    }
}
