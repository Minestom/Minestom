package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.ArgumentParserType;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.utils.StringUtils;

public class ArgumentResourceLocation extends Argument<String> {

    public static final int SPACE_ERROR = 1;

    public ArgumentResourceLocation(String id) {
        super(id);
    }

    @Override
    public String parse(CommandSender sender, String input) throws ArgumentSyntaxException {
        if (input.contains(StringUtils.SPACE))
            throw new ArgumentSyntaxException("Resource location cannot contain space character", input, SPACE_ERROR);

        return input;
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
