package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.builder.arguments.Argument;

public abstract class ArgumentRange<T> extends Argument<T> {

    public static final int FORMAT_ERROR = -1;

    public ArgumentRange(String id) {
        super(id);
    }

    @Override
    public int getConditionResult(T value) {
        return SUCCESS;
    }
}
