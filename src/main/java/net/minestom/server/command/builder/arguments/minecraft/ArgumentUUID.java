package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.CommandReader;
import net.minestom.server.command.builder.arguments.Argument;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ArgumentUUID extends Argument<UUID> {

    public static final int INVALID_UUID = -1;

    public ArgumentUUID(@NotNull String id) {
        super(id);
    }

    @Override
    public @NotNull Result<UUID> parse(CommandReader reader) {
        final String input = reader.readWord();
        try {
            return Result.success(UUID.fromString(input));
        } catch (IllegalArgumentException exception) {
            return Result.syntaxError("Invalid UUID", input, INVALID_UUID);
        }
    }

    @Override
    public String parser() {
        return "minecraft:uuid";
    }

    @Override
    public String toString() {
        return String.format("UUID<%s>", getId());
    }
}
