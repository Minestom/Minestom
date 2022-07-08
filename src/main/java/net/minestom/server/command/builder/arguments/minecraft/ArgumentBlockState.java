package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.CommandReader;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.block.BlockUtils;
import org.jetbrains.annotations.NotNull;

public class ArgumentBlockState extends Argument<Block> {

    public static final int NO_BLOCK = 1;
    public static final int INVALID_BLOCK = 2;
    public static final int INVALID_PROPERTY = 3;
    public static final int INVALID_PROPERTY_VALUE = 4;

    public ArgumentBlockState(@NotNull String id) {
        super(id);
    }

    @Override
    public @NotNull Result<Block> parse(CommandReader reader) throws ArgumentSyntaxException {
        final String input = reader.readWord();
        final int nbtIndex = input.indexOf("[");
        if (nbtIndex == 0)
            return Result.syntaxError("No block type", input, NO_BLOCK);

        if (nbtIndex == -1) {
            // Only block name
            final Block block = Block.fromNamespaceId(input);
            if (block == null)
                return Result.syntaxError("Invalid block type", input, INVALID_BLOCK);
            return Result.success(block);
        } else {
            if (!input.endsWith("]"))
                return Result.syntaxError("Property list need to end with ]", input, INVALID_PROPERTY);
            // Block state
            final String blockName = input.substring(0, nbtIndex);
            Block block = Block.fromNamespaceId(blockName);
            if (block == null)
                return Result.syntaxError("Invalid block type", input, INVALID_BLOCK);

            // Compute properties
            final String query = input.substring(nbtIndex);
            final var propertyMap = BlockUtils.parseProperties(query);
            try {
                return Result.success(block.withProperties(propertyMap));
            } catch (IllegalArgumentException e) {
                return Result.syntaxError("Invalid property values", input, INVALID_PROPERTY_VALUE);
            }
        }
    }

    @Override
    public String parser() {
        return "minecraft:block_state";
    }

    @Override
    public String toString() {
        return String.format("BlockState<%s>", getId());
    }
}
