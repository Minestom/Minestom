package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.CommandReader;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import org.jetbrains.annotations.NotNull;

public class ArgumentResourceLocation extends Argument<String> {

    public ArgumentResourceLocation(@NotNull String id) {
        super(id);
    }

    @Override
    public @NotNull String parse(CommandReader reader) throws ArgumentSyntaxException {
        final String input = reader.getWord();
        reader.consume();
        return input;
    }

    @Override
    public String parser() {
        return "minecraft:resource_location";
    }

    @Override
    public String toString() {
        return String.format("ResourceLocation<%s>", getId());
    }
}
