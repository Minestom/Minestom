package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.StringReader;
import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a boolean value.
 * <p>
 * Example: true
 */
public class ArgumentBoolean extends Argument<Boolean> {

    public ArgumentBoolean(@NotNull String id) {
        super(id);
    }

    @Override
    public @NotNull Boolean parse(@NotNull StringReader input) throws CommandException {
        return input.readBoolean();
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false, false);
        argumentNode.parser = "brigadier:bool";

        nodeMaker.addNodes(new DeclareCommandsPacket.Node[]{argumentNode});
    }

    @Override
    public String toString() {
        return String.format("Boolean<%s>", getId());
    }
}
