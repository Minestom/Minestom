package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.utils.Range;

/**
 * Represents an argument which will give you an {@link Range.Float}.
 * <p>
 * Example: ..3, 3.., 5..10, 15
 */
public class ArgumentFloatRange extends ArgumentRange<Range.Float, Float> {

    public ArgumentFloatRange(String id) {
        super(id, -Float.MAX_VALUE, Float.MAX_VALUE, Float::parseFloat, Range.Float::new);
    }

    @Override
    public String parser() {
        return "minecraft:float_range";
    }

    @Override
    public String toString() {
        return String.format("FloatRange<%s>", getId());
    }
}
