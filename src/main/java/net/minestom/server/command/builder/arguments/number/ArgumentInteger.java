package net.minestom.server.command.builder.arguments.number;

import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import org.jetbrains.annotations.NotNull;

public class ArgumentInteger extends ArgumentNumber<Integer> {

    public ArgumentInteger(String id) {
        super(id);
        this.min = Integer.MIN_VALUE;
        this.max = Integer.MAX_VALUE;
    }

    @NotNull
    @Override
    public Integer parse(@NotNull String input) throws ArgumentSyntaxException {
        try {
            final int value = Integer.parseInt(parseValue(input), getRadix(input));

            // Check range
            if (hasMin && value < min) {
                throw new ArgumentSyntaxException("Input is lower than the minimum required value", input, RANGE_ERROR);
            }
            if (hasMax && value > max) {
                throw new ArgumentSyntaxException("Input is higher than the minimum required value", input, RANGE_ERROR);
            }

            return value;
        } catch (NumberFormatException | NullPointerException e) {
            throw new ArgumentSyntaxException("Input is not a number/long", input, NOT_NUMBER_ERROR);
        }
    }

}
