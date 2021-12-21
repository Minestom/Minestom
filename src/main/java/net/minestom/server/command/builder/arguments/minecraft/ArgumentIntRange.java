package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.FixedStringReader;
import net.minestom.server.command.StringReader;
import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.utils.math.IntRange;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an argument which will give you an {@link IntRange}.
 * <p>
 * Example: ..3, 3.., 5..10, 15
 */
public class ArgumentIntRange extends ArgumentRange<IntRange, Integer> {

    public ArgumentIntRange(@NotNull String id) {
        super(id, "minecraft:int_range");
    }

    @Override
    public @NotNull IntRange parse(@NotNull StringReader input) throws CommandException {
        return ArgumentRange.readNumberRange(input, ArgumentIntRange::parseIntegerFrom, IntRange::new, (min, max) -> min > max);
    }

    private static @NotNull Integer parseIntegerFrom(@NotNull String input, @NotNull FixedStringReader context) {
        if (input.contains(".")) {
            throw CommandException.ARGUMENT_RANGE_INTS.generateException(context.all(), context.position());
        }
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException exception) {
            throw CommandException.PARSING_INT_INVALID.generateException(context.all(), context.position(), input);
        }
    }

    @Override
    public String toString() {
        return String.format("IntRange<%s>", getId());
    }
}
