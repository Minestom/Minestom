package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.jetbrains.annotations.NotNull;

public class ArgumentBlockState extends Argument<Block> {

    public static final int NO_BLOCK = 1;
    public static final int INVALID_BLOCK = 2;
    public static final int INVALID_PROPERTY = 3;

    public ArgumentBlockState(@NotNull String id) {
        super(id);
    }

    @Override
    public @NotNull Block parse(@NotNull String input) throws ArgumentSyntaxException {
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
            final String propertiesString = input.substring(nbtIndex + 1, input.length() - 1);
            StringBuilder keyBuilder = new StringBuilder();
            StringBuilder valueBuilder = new StringBuilder();
            StringBuilder builder = keyBuilder;
            for (int i = 0; i < propertiesString.length(); i++) {
                final char c = propertiesString.charAt(i);
                if (c == '=') {
                    // Switch to value builder
                    builder = valueBuilder;
                } else if (c == ',') {
                    // Append current text
                    block = block.withProperty(keyBuilder.toString(), valueBuilder.toString());
                    keyBuilder = new StringBuilder();
                    valueBuilder = new StringBuilder();
                    builder = keyBuilder;
                } else if (c != ' ') {
                    builder.append(c);
                }
            }
            return block.withProperty(keyBuilder.toString(), valueBuilder.toString());
        }
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false, false);
        argumentNode.parser = "minecraft:block_state";

        nodeMaker.addNodes(new DeclareCommandsPacket.Node[]{argumentNode});
    }

    @Override
    public String toString() {
        return String.format("BlockState<%s>", getId());
    }
}
