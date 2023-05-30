package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.utils.StringUtils;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ArgumentResourceOrTag extends Argument<String> {

    public static final int SPACE_ERROR = 1;

    private final String identifier;

    public ArgumentResourceOrTag(@NotNull String id, @NotNull String identifier) {
        super(id);
        this.identifier = identifier;
    }

    @Override
    public @NotNull String parse(@NotNull String input) throws ArgumentSyntaxException {
        if (input.contains(StringUtils.SPACE))
            throw new ArgumentSyntaxException("Resource location cannot contain space character", input, SPACE_ERROR);

        return input;
    }

    @Override
    public String parser() {
        return "minecraft:resource_or_tag";
    }

    @Override
    public String toString() {
        return String.format("ResourceOrTag<%s>", getId());
    }

    @Override
    public byte @Nullable [] nodeProperties() {
        return BinaryWriter.makeArray(packetWriter ->
                packetWriter.writeSizedString(this.identifier)
        );
    }
}
