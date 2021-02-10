package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.CommandManager;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public class ArgumentLiteral extends Argument<String> {

    public static final int SPACE_ERROR = 1;
    public static final int INVALID_VALUE_ERROR = 2;

    public ArgumentLiteral(@NotNull String id) {
        super(id);
    }

    @NotNull
    @Override
    public String parse(@NotNull String input) throws ArgumentSyntaxException {
        if (input.contains(StringUtils.SPACE))
            throw new ArgumentSyntaxException("Literals cannot contain space character", input, SPACE_ERROR);

        if (!input.equals(getId()))
            throw new ArgumentSyntaxException("Invalid literal value", input, INVALID_VALUE_ERROR);

        return input;
    }

    @NotNull
    @Override
    public DeclareCommandsPacket.Node[] toNodes(boolean executable) {
        DeclareCommandsPacket.Node literalNode = new DeclareCommandsPacket.Node();
        literalNode.flags = COMMAND_MANAGER.getFlag(CommandManager.NodeType.LITERAL, executable, false, false);
        literalNode.name = getId();

        return new DeclareCommandsPacket.Node[]{literalNode};
    }
}
