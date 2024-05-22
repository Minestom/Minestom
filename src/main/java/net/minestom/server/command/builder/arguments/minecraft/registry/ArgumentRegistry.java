package net.minestom.server.command.builder.arguments.minecraft.registry;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import org.jetbrains.annotations.NotNull;

public abstract class ArgumentRegistry<T> extends Argument<T> {

    public static final int INVALID_NAME = -2;

    ArgumentRegistry(String id) {
        super(id);
    } //Microtus - update java keyword usage

    public abstract T getRegistry(@NotNull String value);

    @NotNull
    @Override
    public T parse(@NotNull CommandSender sender, @NotNull String input) throws ArgumentSyntaxException {
        final T registryValue = getRegistry(input);
        if (registryValue == null)
            throw new ArgumentSyntaxException("Registry value is invalid", input, INVALID_NAME);

        return registryValue;
    }
}
