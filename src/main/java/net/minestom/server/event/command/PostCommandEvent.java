package net.minestom.server.event.command;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandResult;
import net.minestom.server.event.trait.CancellableEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called after a command is processed by the command manager
 */
public class PostCommandEvent implements CommandEvent {

    private final CommandSender sender;
    private final String command;
    private final CommandResult commandResult;

    public PostCommandEvent(
            @NotNull CommandSender sender,
            @NotNull String command,
            @NotNull CommandResult commandResult
    ) {
        this.sender = sender;
        this.command = command;
        this.commandResult = commandResult;
    }

    @Override
    public @NotNull CommandSender getSender() {
        return sender;
    }

    /**
     * Gets the command used (command name + arguments).
     *
     * @return the command that the player wants to execute
     */
    public @NotNull String getCommand() {
        return command;
    }

    /**
     * Gets the command result from the command.
     * @return The command result from the command.
     */
    public @NotNull CommandResult getCommandResult() {
        return commandResult;
    }
}