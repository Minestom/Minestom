package net.minestom.server.command.builder.arguments.minecraft.registry;

import net.minestom.server.MinecraftServer;
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

    @NotNull
    @Override
    public DeclareCommandsPacket.Node[] toNodes(boolean executable) {
        DeclareCommandsPacket.Node argumentNode = MinecraftServer.getCommandManager().simpleArgumentNode(this, executable, false);
        argumentNode.parser = "minecraft:block_state";
        return new DeclareCommandsPacket.Node[]{argumentNode};
    }
}
