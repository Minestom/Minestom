package net.minestom.server.command.builder.arguments.minecraft.registry;

import net.minestom.server.command.builder.arguments.Argument;
import org.jetbrains.annotations.NotNull;

public abstract class ArgumentRegistry<T> extends Argument<T> {

    public static final int INVALID_NAME = -2;

    public ArgumentRegistry(String id) {
        super(id);
    }

    public abstract T getRegistry(String value);

    @Override
    public int getCorrectionResult(@NotNull String value) {
        return getRegistry(value) == null ? INVALID_NAME : SUCCESS;
    }

    @NotNull
    @Override
    public T parse(@NotNull String value) {
        return getRegistry(value);
    }

    @Override
    public int getConditionResult(@NotNull T value) {
        return SUCCESS;
    }
}
