package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.CommandReader;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import org.jetbrains.annotations.NotNull;

public class ArgumentLiteral extends Argument<String> {

    public static final int INVALID_VALUE_ERROR = 1;

    public ArgumentLiteral(@NotNull String id) {
        super(id);
    }

    @Override
    public @NotNull String parse(CommandReader reader) throws ArgumentSyntaxException {
        final String word = reader.getWord();

        if (!word.equals(getId()))
            throw new ArgumentSyntaxException("Invalid literal value", word, INVALID_VALUE_ERROR);

        reader.consume();
        return word;
    }

    @Override
    public String parser() {
        return null;
    }

    @Override
    public String toString() {
        return String.format("Literal<%s>", getId());
    }
}
