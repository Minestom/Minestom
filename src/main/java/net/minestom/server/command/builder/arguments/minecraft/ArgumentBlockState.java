package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.StringReader;
import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.jetbrains.annotations.NotNull;

public class ArgumentBlockState extends Argument<Block> {

    public static final char START_PROPERTIES = '[', END_PROPERTIES = ']';

    public ArgumentBlockState(@NotNull String id) {
        super(id);
    }

    @Override
    public @NotNull Block parse(@NotNull StringReader input) throws CommandException {
        // FIXME: This has not been implemented because Hephaistos does not support reading select amounts of a reader yet.
        throw CommandException.COMMAND_UNKNOWN_ARGUMENT.generateException(input.all(), input.position());
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false, false);
        argumentNode.parser = "minecraft:block_state";

        nodeMaker.addNodes(argumentNode);
    }

    /**
     * @deprecated use {@link Argument#parse(Argument)}
     */
    @Deprecated
    public static Block staticParse(@NotNull String input) throws ArgumentSyntaxException {
        final int nbtIndex = input.indexOf("[");
        if (nbtIndex == 0)
            throw new ArgumentSyntaxException("No block type", input, NO_BLOCK);

        if (nbtIndex == -1) {
            // Only block name
            final Block block = Block.fromNamespaceId(input);
            if (block == null)
                throw new ArgumentSyntaxException("Invalid block type", input, INVALID_BLOCK);
            return block;
        } else {
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
    public String toString() {
        return String.format("BlockState<%s>", getId());
    }
}
