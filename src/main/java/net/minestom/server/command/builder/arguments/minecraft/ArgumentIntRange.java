package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.ArgumentParserType;
import net.minestom.server.utils.Range;

/**
 * Represents an argument which will give you an {@link Range.Int}.
 * <p>
 * Example: ..3, 3.., 5..10, 15
 */
public class ArgumentIntRange extends ArgumentRange<Range.Int, Integer> {

    public ArgumentIntRange(String id) {
        super(id, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer::parseInt, Range.Int::new);
    }

    @Override
    public ArgumentParserType parser() {
        return ArgumentParserType.INT_RANGE;
    }

    @Override
    public String toString() {
        return String.format("IntRange<%s>", getId());
    }
}
