package net.minestom.server.command.builder.arguments.minecraft.registry;

import net.minestom.server.command.builder.arguments.Argument;

public abstract class ArgumentRegistry<T> extends Argument<T> {

    public static final int INVALID_NAME = -2;

    public ArgumentRegistry(String id) {
        super(id);
    }

    public abstract T getRegistry(String value);

    @Override
    public int getCorrectionResult(String value) {
        return getRegistry(value) == null ? INVALID_NAME : SUCCESS;
    }

    @Override
    public T parse(String value) {
        return getRegistry(value);
    }

    @Override
    public int getConditionResult(T value) {
        return SUCCESS;
    }
}
