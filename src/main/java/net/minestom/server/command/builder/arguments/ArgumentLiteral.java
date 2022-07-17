package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.CommandReader;
import org.jetbrains.annotations.NotNull;

public class ArgumentLiteral extends Argument<String> {

    public ArgumentLiteral(@NotNull String id) {
        super(id);
    }

    @Override
    public @NotNull Result<String> parse(CommandReader reader) {
        final String word = reader.readWord();

        if (!word.equals(getId()))
            return Result.incompatibleType();

        return Result.success(word);
    }

    @Override
    public String parser() {
        return null;
    }

    @Override
    public String toString() {
        return String.format("Literal<%s>", getId());
    }
}
