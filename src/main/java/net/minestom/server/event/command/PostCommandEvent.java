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

    public PostCommandEvent(
            @NotNull CommandSender sender,
            @NotNull String command,
            @NotNull CommandResult commandResult
    ) {
        this.sender = sender;
        this.command = command;
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
    @Override
    public @NotNull String getCommand() {
        return command;
    }
}