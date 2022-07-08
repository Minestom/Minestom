package net.minestom.server.command.builder.arguments.minecraft;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minestom.server.command.CommandReader;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import org.jetbrains.annotations.NotNull;

public class ArgumentComponent extends Argument<Component> {

    public static final int INVALID_JSON_ERROR = 1;

    public ArgumentComponent(@NotNull String id) {
        super(id);
    }

    @Override
    public @NotNull Result<Component> parse(CommandReader reader) throws ArgumentSyntaxException {
        final char start = reader.peekNextChar();
        if (start != '{') Result.incompatibleType();
        final int end = reader.getClosingIndexOfJsonObject(0);

        if (end == -1) {
            return Result.syntaxError("Invalid JSON", reader.getRemaining(), INVALID_JSON_ERROR);
        }

        final String s = reader.read(end);
        try {
            return Result.success(GsonComponentSerializer.gson().deserialize(s));
        } catch (Exception e) {
            return Result.syntaxError("Invalid component", s, INVALID_JSON_ERROR);
        }
    }

    @Override
    public String parser() {
        return "minecraft:component";
    }

    @Override
    public String toString() {
        return String.format("Component<%s>", getId());
    }
}
