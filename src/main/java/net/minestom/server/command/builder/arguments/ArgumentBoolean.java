package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.CommandReader;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a boolean value.
 * <p>
 * Example: true
 */
public class ArgumentBoolean extends Argument<Boolean> {
    public ArgumentBoolean(String id) {
        super(id);
    }

    @Override
    public @NotNull Result<Boolean> parse(CommandReader reader) throws ArgumentSyntaxException {
        final String word = reader.readWord();

        if (word.equalsIgnoreCase("true")) {
            return Result.success(true);
        }
        if (word.equalsIgnoreCase("false")) {
            return Result.success(false);
        }

        return Result.incompatibleType();
    }

    @Override
    public String parser() {
        return "brigadier:bool";
    }
    @Override
    public String toString() {
        return String.format("Boolean<%s>", getId());
    }
}
