package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.StringReader;
import net.minestom.server.command.builder.CommandResult;
import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class ArgumentCommand extends Argument<CommandResult> {

    private boolean onlyCorrect;
    private String shortcut = "";

    public ArgumentCommand(@NotNull String id) {
        super(id);
    }

    @Override
    public @NotNull CommandResult parse(@NotNull StringReader input) throws CommandException {
        // FIXME: Complete
        throw CommandException.COMMAND_UNKNOWN_ARGUMENT.generateException(input);
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

    @Contract("_ -> this")
    public @NotNull ArgumentCommand setOnlyCorrect(boolean onlyCorrect) {
        this.onlyCorrect = onlyCorrect;
        return this;
    }

    @NotNull
    public String getShortcut() {
        return shortcut;
    }

    @ApiStatus.Experimental
    @Contract("_ -> this")
    public @NotNull ArgumentCommand setShortcut(@NotNull String shortcut) {
        this.shortcut = shortcut;
        return this;
    }

    @Override
    public String toString() {
        return String.format("Command<%s>", getId());
    }
}
