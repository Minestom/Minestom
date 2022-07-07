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

    public static final int NOT_BOOLEAN_ERROR = 1;

    public ArgumentBoolean(String id) {
        super(id);
    }

    @Override
    public @NotNull Boolean parse(CommandReader reader) throws ArgumentSyntaxException {
        final String word = reader.getWord();

        if (word.equalsIgnoreCase("true")) {
            reader.consume();
            return true;
        }
        if (word.equalsIgnoreCase("false")) {
            reader.consume();
            return false;
        }

        throw new ArgumentSyntaxException("Not a boolean", word, NOT_BOOLEAN_ERROR);
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
