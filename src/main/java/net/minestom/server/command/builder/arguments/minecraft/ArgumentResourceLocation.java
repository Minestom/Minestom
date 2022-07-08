package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.CommandReader;
import net.minestom.server.command.builder.arguments.Argument;
import org.jetbrains.annotations.NotNull;

public class ArgumentResourceLocation extends Argument<String> {

    public ArgumentResourceLocation(@NotNull String id) {
        super(id);
    }

    @Override
    public @NotNull Result<String> parse(CommandReader reader) {
        //todo shouldn't this have some syntax checks?
        return Result.success(reader.readWord());
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
