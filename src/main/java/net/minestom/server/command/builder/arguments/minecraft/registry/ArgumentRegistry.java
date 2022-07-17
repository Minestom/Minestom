package net.minestom.server.command.builder.arguments.minecraft.registry;

import net.minestom.server.command.CommandReader;
import net.minestom.server.command.builder.arguments.Argument;
import org.jetbrains.annotations.NotNull;

public abstract class ArgumentRegistry<T> extends Argument<T> {

    public static final int INVALID_NAME = -2;

    public ArgumentRegistry(String id) {
        super(id);
    }

    public abstract T getRegistry(@NotNull String value);

    @Override
    public @NotNull Result<T> parse(CommandReader reader) {
        final String input = reader.readWord();
        final T registryValue = getRegistry(input);
        if (registryValue == null)
            return Result.syntaxError("Registry value is invalid", input, INVALID_NAME);
        //fixme check vanilla incompatible/syntax error and in every other arg too

        return Result.success(registryValue);
    }
}
