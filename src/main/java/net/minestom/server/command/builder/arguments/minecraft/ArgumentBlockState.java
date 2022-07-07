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
    public @NotNull Block parse(CommandReader reader) throws ArgumentSyntaxException {
        final String input = reader.getWord();
        final int nbtIndex = input.indexOf("[");
        if (nbtIndex == 0)
            throw new ArgumentSyntaxException("No block type", input, NO_BLOCK);

        if (nbtIndex == -1) {
            // Only block name
            final Block block = Block.fromNamespaceId(input);
            if (block == null)
                throw new ArgumentSyntaxException("Invalid block type", input, INVALID_BLOCK);
            reader.consume();
            return block;
        } else {
            reader.consume();
            if (!input.endsWith("]"))
                throw new ArgumentSyntaxException("Property list need to end with ]", input, INVALID_PROPERTY);
            // Block state
            final String blockName = input.substring(0, nbtIndex);
            Block block = Block.fromNamespaceId(blockName);
            if (block == null)
                throw new ArgumentSyntaxException("Invalid block type", input, INVALID_BLOCK);

            // Compute properties
            final String query = input.substring(nbtIndex);
            final var propertyMap = BlockUtils.parseProperties(query);
            try {
                return block.withProperties(propertyMap);
            } catch (IllegalArgumentException e) {
                throw new ArgumentSyntaxException("Invalid property values", input, INVALID_PROPERTY_VALUE);
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
