package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.ArgumentParserType;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;

import java.util.UUID;

public class ArgumentUUID extends Argument<UUID> {

    public static final int INVALID_UUID = -1;

    public ArgumentUUID(String id) {
        super(id);
    }

    @Override
    public UUID parse(CommandSender sender, String input) throws ArgumentSyntaxException {
        try {
            return UUID.fromString(input);
        } catch (IllegalArgumentException exception) {
            throw new ArgumentSyntaxException("Invalid UUID", input, INVALID_UUID);
        }
    }

    @Override
    public ArgumentParserType parser() {
        return ArgumentParserType.UUID;
    }

    @Override
    public String toString() {
        return String.format("UUID<%s>", getId());
    }
}
