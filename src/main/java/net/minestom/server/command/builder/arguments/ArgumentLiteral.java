package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.ArgumentParserType;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import org.jetbrains.annotations.NotNull;

public class ArgumentLiteral extends Argument<String> {

    public static final int INVALID_VALUE_ERROR = 1;

    public ArgumentLiteral(@NotNull String id) {
        super(id);
    }

    @NotNull
    @Override
    public String parse(@NotNull CommandSender sender, @NotNull String input) throws ArgumentSyntaxException {
        if (!input.equals(getId()))
            throw new ArgumentSyntaxException("Invalid literal value", input, INVALID_VALUE_ERROR);

        return input;
    }

    @Override
    public ArgumentParserType parser() {
        return null;
    }

    @Override
    public String toString() {
        return String.format("Literal<%s>", getId());
    }
}
