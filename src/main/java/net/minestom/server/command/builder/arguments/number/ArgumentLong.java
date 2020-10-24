package net.minestom.server.command.builder.arguments.number;

import org.jetbrains.annotations.NotNull;

public class ArgumentLong extends ArgumentNumber<Long> {

    public ArgumentLong(String id) {
        super(id);
        this.min = Long.MIN_VALUE;
        this.max = Long.MAX_VALUE;
    }

    @Override
    public int getCorrectionResult(@NotNull String value) {
        try {
            Long.parseLong(parseValue(value), getRadix(value));
            return SUCCESS;
        } catch (NumberFormatException | NullPointerException e) {
            return NOT_NUMBER_ERROR;
        }
    }

    @NotNull
    @Override
    public Long parse(@NotNull String value) {
        return Long.parseLong(parseValue(value), getRadix(value));
    }

    @Override
    public int getConditionResult(@NotNull Long value) {
        // Check range
        if (hasMin && value < min) {
            return RANGE_ERROR;
        }
        if (hasMax && value > max) {
            return RANGE_ERROR;
        }

        return SUCCESS;
    }

}
