package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.utils.time.UpdateOption;

import java.util.Arrays;
import java.util.List;

/**
 * Represent an argument giving a time (day/second/tick)
 * Chat format: 50d, 25s, 75t
 */
public class ArgumentTime extends Argument<UpdateOption> {

    public static final int INVALID_TIME_FORMAT = -2;
    public static final int NO_NUMBER = -3;
    private static final List<Character> suffixes = Arrays.asList('d', 's', 't');

    public ArgumentTime(String id) {
        super(id);
    }

    @Override
    public int getCorrectionResult(String value) {
        final char lastChar = value.charAt(value.length() - 1);
        if (!suffixes.contains(lastChar))
            return INVALID_TIME_FORMAT;
        value = value.substring(0, value.length() - 1);
        try {
            // Check if value is a number
            Integer.parseInt(value);

            return SUCCESS;
        } catch (NumberFormatException e) {
            return NO_NUMBER;
        }
    }

    @Override
    public UpdateOption parse(String value) {
        final char lastChar = value.charAt(value.length() - 1);
        TimeUnit timeUnit = null;
        if (lastChar == 'd') {
            timeUnit = TimeUnit.DAY;
        } else if (lastChar == 's') {
            timeUnit = TimeUnit.SECOND;
        } else if (lastChar == 't') {
            timeUnit = TimeUnit.TICK;
        }
        value = value.substring(0, value.length() - 1);
        final int time = Integer.parseInt(value);

        return new UpdateOption(time, timeUnit);
    }

    @Override
    public int getConditionResult(UpdateOption value) {
        return SUCCESS;
    }
}
