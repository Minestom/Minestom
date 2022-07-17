package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.CommandReader;
import net.minestom.server.command.builder.arguments.Argument;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class ArgumentResourceLocation extends Argument<String> {

    public static final Pattern RESOURCE_REGEX = Pattern.compile("([a-z\\d_\\-.]+:)?[a-z\\d_\\-./]+");

    public ArgumentResourceLocation(@NotNull String id) {
        super(id);
    }

    @Override
    public @NotNull Result<String> parse(CommandReader reader) {
        final String word = reader.readWord();
        if (RESOURCE_REGEX.matcher(word).matches()) {
            return Result.success(word);
        } else {
            return Result.syntaxError("Invalid resource location format", word, -1);
        }
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
