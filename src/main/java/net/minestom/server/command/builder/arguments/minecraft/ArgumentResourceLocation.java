package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.StringReader;
import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

public class ArgumentResourceLocation extends Argument<NamespaceID> {

    public ArgumentResourceLocation(@NotNull String id) {
        super(id);
    }

    @Override
    public @NotNull NamespaceID parse(@NotNull StringReader input) throws CommandException {
        return input.readNamespaceID();
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false, false);
        argumentNode.parser = "minecraft:resource_location";

        nodeMaker.addNodes(new DeclareCommandsPacket.Node[]{argumentNode});
    }

    @Override
    public String toString() {
        return String.format("ResourceLocation<%s>", getId());
    }
}
