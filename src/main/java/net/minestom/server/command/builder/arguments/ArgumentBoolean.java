package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.ArgumentParserType;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a boolean value.
 * <p>
 * Example: true
 */
public class ArgumentBoolean extends Argument<Boolean> {

    public static final int NOT_BOOLEAN_ERROR = 1;

    public ArgumentBoolean(String id) {
        super(id);
    }

    @NotNull
    @Override
    public Boolean parse(@NotNull CommandSender sender, @NotNull String input) throws ArgumentSyntaxException {
        if (input.equalsIgnoreCase("true"))
            return true;
        if (input.equalsIgnoreCase("false"))
            return false;

        throw new ArgumentSyntaxException("Not a boolean", input, NOT_BOOLEAN_ERROR);
    }

    @Override
    public ArgumentParserType parser() {
        return ArgumentParserType.BOOL;
    }
    @Override
    public String toString() {
        return String.format("Boolean<%s>", getId());
    }
}
