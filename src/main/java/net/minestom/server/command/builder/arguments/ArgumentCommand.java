package net.minestom.server.command.builder.arguments;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.StringReader;
import net.minestom.server.command.builder.CommandResult;
import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class ArgumentCommand extends Argument<CommandResult> {

    private boolean onlyCorrect;

    public ArgumentCommand(@NotNull String id) {
        super(id);
    }

    @Override
    public @NotNull CommandResult parse(@NotNull StringReader input) throws CommandException {
        int start = input.position();
        CommandResult result = MinecraftServer.getCommandManager().getDispatcher().parse(input);

        if (onlyCorrect && result.type() != CommandResult.Type.SUCCESS) {
            if (result.parsedCommand() == null || result.parsedCommand().getException() == null) {
                throw CommandException.COMMAND_EXCEPTION.generateException(input.all(), start, input.all().substring(start));
            }
            throw result.parsedCommand().getException();
        }

        return result;
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        for (DeclareCommandsPacket.Node node : nodeMaker.getLatestNodes()) {
            node.flags |= 0x08; // Redirection mask
            node.redirectedNode = 0; // Redirect to root
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

    @Override
    public String toString() {
        return String.format("Command<%s>", getId());
    }
}
