package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.builder.arguments.Argument;

// FIXME: cannot make the minecraft:range identifier working
public class ArgumentRange extends Argument<Float> {

    public ArgumentRange(String id, boolean allowSpace, boolean useRemaining) {
        super(id, allowSpace, useRemaining);
    }

    @Override
    public int getCorrectionResult(String value) {
        return 0;
    }

    @Override
    public Float parse(String value) {
        return null;
    }

    @Override
    public int getConditionResult(Float value) {
        return 0;
    }
}
