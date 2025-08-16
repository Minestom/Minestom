package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.ArgumentParserType;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;

public class ArgumentLiteral extends Argument<String> {

    public static final int INVALID_VALUE_ERROR = 1;

    public ArgumentLiteral(String id) {
        super(id);
    }

    @Override
    public String parse(CommandSender sender, String input) throws ArgumentSyntaxException {
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
