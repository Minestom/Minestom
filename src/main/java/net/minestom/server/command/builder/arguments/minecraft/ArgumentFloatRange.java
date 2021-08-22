package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.utils.math.FloatRange;

/**
 * Represents an argument which will give you an {@link FloatRange}.
 * <p>
 * Example: ..3, 3.., 5..10, 15
 */
public class ArgumentFloatRange extends ArgumentRange<FloatRange, Float> {

    public ArgumentFloatRange(String id) {
        super(id, "minecraft:float_range", Float.MIN_VALUE, Float.MAX_VALUE, Float::parseFloat, FloatRange::new);
    }

    @Override
    public String toString() {
        return String.format("FloatRange<%s>", getId());
    }
}
