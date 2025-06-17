package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.ArgumentParserType;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

public class ArgumentBlockState extends Argument<Block> {

    public static final int NO_BLOCK = 1;
    public static final int INVALID_BLOCK = 2;
    public static final int INVALID_PROPERTY = 3;
    public static final int INVALID_PROPERTY_VALUE = 4;

    public ArgumentBlockState(@NotNull String id) {
        super(id, true, false);
    }

    @Override
    public @NotNull Block parse(@NotNull CommandSender sender, @NotNull String input) throws ArgumentSyntaxException {
        return staticParse(input);
    }

    @Override
    public @NotNull ArgumentParserType parser() {
        return ArgumentParserType.BLOCK_STATE;
    }

    /**
     * @deprecated use {@link Argument#parse(CommandSender, Argument)}
     */
    @Deprecated
    public static Block staticParse(@NotNull String input) throws ArgumentSyntaxException {
        return Block.fromState(input);
    }

    @Override
    public String toString() {
        return String.format("BlockState<%s>", getId());
    }
}
