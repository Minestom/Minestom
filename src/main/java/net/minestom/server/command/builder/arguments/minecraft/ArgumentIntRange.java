package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.utils.math.IntRange;

/**
 * Represents an argument which will give you an {@link IntRange}.
 * <p>
 * Example: ..3, 3.., 5..10, 15
 */
public class ArgumentIntRange extends ArgumentRange<IntRange, Integer> {

    public ArgumentIntRange(String id) {
        super(id, "minecraft:int_range", Integer.MIN_VALUE, Integer.MAX_VALUE, Integer::parseInt, IntRange::new);
    }

    @Override
    public String toString() {
        return String.format("IntRange<%s>", getId());
    }
}
