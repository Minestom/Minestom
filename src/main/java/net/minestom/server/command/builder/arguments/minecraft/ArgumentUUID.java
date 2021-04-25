package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ArgumentUUID extends Argument<UUID> {

    public static final int INVALID_UUID = -1;

    public ArgumentUUID(@NotNull String id) {
        super(id);
    }

    @NotNull
    @Override
    public UUID parse(@NotNull String input) throws ArgumentSyntaxException {
        try {
            return UUID.fromString(input);
        } catch (IllegalArgumentException exception) {
            throw new ArgumentSyntaxException("Invalid UUID", input, INVALID_UUID);
        }
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
