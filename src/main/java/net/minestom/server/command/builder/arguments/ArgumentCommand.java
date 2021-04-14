package net.minestom.server.command.builder.arguments;

import com.google.common.annotations.Beta;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.CommandDispatcher;
import net.minestom.server.command.builder.CommandResult;
import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public class ArgumentCommand extends Argument<CommandResult> {

    public static final int INVALID_COMMAND_ERROR = 1;

    private boolean onlyCorrect;
    private String shortcut = "";

    public ArgumentCommand(@NotNull String id) {
        super(id, true, true);
    }

    @NotNull
    @Override
    public CommandResult parse(@NotNull String input) throws ArgumentSyntaxException {
        final String commandString = !shortcut.isEmpty() ?
                shortcut + StringUtils.SPACE + input
                : input;
        CommandDispatcher dispatcher = MinecraftServer.getCommandManager().getDispatcher();
        CommandResult result = dispatcher.parse(commandString);

        if (onlyCorrect && result.getType() != CommandResult.Type.SUCCESS)
            throw new ArgumentSyntaxException("Invalid command", input, INVALID_COMMAND_ERROR);

        return result;
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        final DeclareCommandsPacket.Node[] lastNodes = nodeMaker.getLatestNodes();

        if (!shortcut.isEmpty()) {
            nodeMaker.request(shortcut, (id) -> {
                for (DeclareCommandsPacket.Node node : lastNodes) {
                    node.flags |= 0x08; // Redirection mask
                    node.redirectedNode = id;
                }
            });
        } else {
            for (DeclareCommandsPacket.Node node : lastNodes) {
                node.flags |= 0x08; // Redirection mask
                node.redirectedNode = 0; // Redirect to root
            }
        }
    }

    public boolean isOnlyCorrect() {
        return onlyCorrect;
    }

    public ArgumentCommand setOnlyCorrect(boolean onlyCorrect) {
        this.onlyCorrect = onlyCorrect;
        return this;
    }

    @NotNull
    public String getShortcut() {
        return shortcut;
    }

    @Beta
    public ArgumentCommand setShortcut(@NotNull String shortcut) {
        this.shortcut = shortcut;
        return this;
    }
}
