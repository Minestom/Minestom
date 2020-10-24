package net.minestom.server.command.builder.arguments.number;

import org.jetbrains.annotations.NotNull;

public class ArgumentDouble extends ArgumentNumber<Double> {

    public ArgumentDouble(String id) {
        super(id);
        this.min = Double.MIN_VALUE;
        this.max = Double.MAX_VALUE;
    }

    @Override
    public int getCorrectionResult(@NotNull String value) {
        try {
            String parsed = parseValue(value);
            int radix = getRadix(value);
            if (radix != 10) {
                Long.parseLong(parsed, radix);
            } else {
                Double.parseDouble(parsed);
            }
            return SUCCESS;
        } catch (NumberFormatException | NullPointerException e) {
            return NOT_NUMBER_ERROR;
        }
    }

    @NotNull
    @Override
    public Double parse(@NotNull String value) {
        String parsed = parseValue(value);
        int radix = getRadix(value);
        if (radix != 10) {
            return (double) Long.parseLong(parsed, radix);
        }
        return Double.parseDouble(parsed);
    }

    @Override
    public int getConditionResult(@NotNull Double value) {
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
