package net.minestom.server.event.command;

import net.minestom.server.command.CommandSender;
import net.minestom.server.event.trait.CancellableEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called before a command is processed by the command manager
 */
public class PreCommandEvent implements CommandEvent, CancellableEvent {

    private final CommandSender sender;
    private String command;
    private boolean cancelled;

    public PreCommandEvent(@NotNull CommandSender sender, @NotNull String command) {
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

    public void setCommand(@NotNull String command) {
        this.command = command;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}