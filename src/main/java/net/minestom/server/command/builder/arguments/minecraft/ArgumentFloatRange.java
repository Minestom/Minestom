package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.FixedStringReader;
import net.minestom.server.command.StringReader;
import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.utils.math.FloatRange;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an argument which will give you an {@link FloatRange}.
 * <p>
 * Example: ..3, 3.., 5..10, 15
 */
public class ArgumentFloatRange extends ArgumentRange<FloatRange, Float> {

    public ArgumentFloatRange(@NotNull String id) {
        super(id, "minecraft:float_range", Float.MIN_VALUE, Float.MAX_VALUE, Float::parseFloat, FloatRange::new);
    }

    @Override
    public @NotNull FloatRange parse(@NotNull StringReader input) throws CommandException {
        return ArgumentRange.readNumberRange(input, ArgumentFloatRange::parseFloatFrom, FloatRange::new, (min, max) -> min > max);
    }

    private static @NotNull Float parseFloatFrom(@NotNull String input, @NotNull FixedStringReader context) {
        try {
            return Float.parseFloat(input);
        } catch (NumberFormatException exception) {
            throw CommandException.PARSING_INT_INVALID.generateException(context, input);
        }
    }

    @Override
    public String toString() {
        return String.format("FloatRange<%s>", getId());
    }
}
