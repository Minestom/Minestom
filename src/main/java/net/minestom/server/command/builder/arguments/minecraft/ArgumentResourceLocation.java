package net.minestom.server.command.builder.arguments.minecraft;

import net.kyori.adventure.key.KeyPattern;
import net.minestom.server.command.ArgumentParserType;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.kyori.adventure.key.Key;

public class ArgumentResourceLocation extends Argument<Key> {

    public static final int PARSE_ERROR = 1;

    public ArgumentResourceLocation(String id) {
        super(id);
    }

    @Override
    public Key parse(CommandSender sender, @KeyPattern String input) throws ArgumentSyntaxException {
        if (!Key.parseable(input))
            throw new ArgumentSyntaxException("Invalid resource location", input, PARSE_ERROR);

        return Key.key(input);
    }

    @Override
    public ArgumentParserType parser() {
        return ArgumentParserType.RESOURCE_LOCATION;
    }

    @Override
    public String toString() {
        return String.format("ResourceLocation<%s>", getId());
    }
}
