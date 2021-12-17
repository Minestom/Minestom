package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.StringReader;
import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.jetbrains.annotations.NotNull;

public class ArgumentLiteral extends Argument<String> {

    public ArgumentLiteral(@NotNull String id) {
        super(id);
    }

    @Override
    public @NotNull String parse(@NotNull StringReader input) throws CommandException {
        String value = input.readUnquotedString();
        if (!value.equals(getId())) {
            throw CommandException.COMMAND_UNKNOWN_ARGUMENT.generateException(input);
        }
        return value;
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        DeclareCommandsPacket.Node literalNode = new DeclareCommandsPacket.Node();
        literalNode.flags = DeclareCommandsPacket.getFlag(DeclareCommandsPacket.NodeType.LITERAL,
                executable, false, false);
        literalNode.name = getId();

        nodeMaker.addNodes(new DeclareCommandsPacket.Node[]{literalNode});
    }

    @Override
    public String toString() {
        return String.format("Literal<%s>", getId());
    }
}
