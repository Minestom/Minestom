package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.StringReader;
import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ArgumentUUID extends Argument<UUID> {

    public ArgumentUUID(@NotNull String id) {
        super(id);
    }

    @Override
    public @NotNull UUID parse(@NotNull StringReader input) throws CommandException {
        return input.readUUID();
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false, false);
        argumentNode.parser = "minecraft:uuid";

        nodeMaker.addNodes(new DeclareCommandsPacket.Node[]{argumentNode});
    }

    @Override
    public String toString() {
        return String.format("UUID<%s>", getId());
    }
}
