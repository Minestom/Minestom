package net.minestom.server.command.builder.arguments.minecraft.registry;

import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.registry.Registries;
import org.jetbrains.annotations.NotNull;

public class ArgumentBlockState extends ArgumentRegistry<Block> {

    public ArgumentBlockState(@NotNull String id) {
        super(id);
    }

    @Override
    public Block getRegistry(@NotNull String value) {
        return Registries.getBlock(value);
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false, false);
        argumentNode.parser = "minecraft:block_state";

        nodeMaker.addNodes(new DeclareCommandsPacket.Node[]{argumentNode});
    }
}
