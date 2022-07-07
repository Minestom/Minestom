package net.minestom.server.command.builder.arguments.minecraft.registry;

import net.minestom.server.command.CommandReader;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import org.jetbrains.annotations.NotNull;

public abstract class ArgumentRegistry<T> extends Argument<T> {

    public static final int INVALID_NAME = -2;

    public ArgumentRegistry(String id) {
        super(id);
    }

    public abstract T getRegistry(@NotNull String value);

    @Override
    public @NotNull T parse(CommandReader reader) throws ArgumentSyntaxException {
        final String input = reader.getWord();
        final T registryValue = getRegistry(input);
        if (registryValue == null)
            throw new ArgumentSyntaxException("Registry value is invalid", input, INVALID_NAME);

        reader.consume();
        return registryValue;
    }
}
