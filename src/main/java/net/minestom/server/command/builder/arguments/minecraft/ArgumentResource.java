package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ArgumentResource extends Argument<String> {

    public static final int SPACE_ERROR = 1;

    private final String identifier;

    public ArgumentResource(@NotNull String id, @NotNull String identifier) {
        super(id);
        this.identifier = identifier;
    }

    @Override
    public @NotNull String parse(@NotNull CommandSender sender, @NotNull String input) throws ArgumentSyntaxException {
        if (input.contains(StringUtils.SPACE))
            throw new ArgumentSyntaxException("Resource location cannot contain space character", input, SPACE_ERROR);

        return input;
    }

    @Override
    public String parser() {
        return "minecraft:resource";
    }

    @Override
    public String toString() {
        return String.format("Resource<%s>", getId());
    }

    @Override
    public byte @Nullable [] nodeProperties() {
        return NetworkBuffer.makeArray(buffer -> buffer.write(NetworkBuffer.STRING, identifier));
    }
}
