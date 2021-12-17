package net.minestom.server.command.builder.arguments.minecraft.registry;

import net.minestom.server.command.StringReader;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ArgumentRegistry<T> extends Argument<T> {

    public ArgumentRegistry(@NotNull String id) {
        super(id);
    }

    public abstract @Nullable T getRegistry(@NotNull String key);

    public abstract @NotNull CommandException createException(@NotNull StringReader input, @NotNull String id);

    @Override
    public @NotNull T parse(@NotNull StringReader input) throws CommandException {
        NamespaceID id = input.readNamespaceID();
        T value = getRegistry(id.asString());

        if (value == null) {
            throw createException(input, id.asString());
        }
        return value;
    }
}
