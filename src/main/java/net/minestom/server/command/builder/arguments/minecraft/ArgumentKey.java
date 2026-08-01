package net.minestom.server.command.builder.arguments.minecraft;

import net.kyori.adventure.key.KeyPattern;
import net.minestom.server.command.ArgumentParserType;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.kyori.adventure.key.Key;

/**
 * Represents a {@link Key} value.
 * <p>
 *     Example: {@code minecraft:air}
 * </p>
 */
public class ArgumentKey extends Argument<Key> {

    public static final int PARSE_ERROR = 1;

    public ArgumentKey(String id) {
        super(id);
    }

    @Override
    public Key parse(CommandSender sender, @KeyPattern String input) throws ArgumentSyntaxException {
        if (!Key.parseable(input))
            throw new ArgumentSyntaxException("Invalid key", input, PARSE_ERROR);

        return Key.key(input);
    }

    @Override
    public ArgumentParserType parser() {
        return ArgumentParserType.RESOURCE_LOCATION;
    }

    @Override
    public String toString() {
        return String.format("Key<%s>", getId());
    }
}
