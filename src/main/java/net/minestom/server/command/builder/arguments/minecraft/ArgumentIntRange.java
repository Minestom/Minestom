package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.utils.Range;

/**
 * Represents an argument which will give you an {@link Range.Integer}.
 * <p>
 * Example: ..3, 3.., 5..10, 15
 */
public class ArgumentIntRange extends ArgumentRange<Range.Integer, Integer> {

    public ArgumentIntRange(String id) {
        super(id, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer::parseInt, Range.Integer::new);
    }

    @Override
    public String parser() {
        return "minecraft:int_range";
    }

    @Override
    public String toString() {
        return String.format("IntRange<%s>", getId());
    }
}
