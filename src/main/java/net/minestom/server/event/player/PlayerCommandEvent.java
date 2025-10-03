package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;

/**
 * Called every time a player send a message starting by '/'.
 */
public class PlayerCommandEvent implements PlayerInstanceEvent, CancellableEvent {

    private final Player player;
    private String command;

    private boolean cancelled;

    public PlayerCommandEvent(Player player, String command) {
        this.player = player;
        this.command = command;
    }

    /**
     * Gets the command used (command name + arguments).
     *
     * @return the command that the player wants to execute
     */
    public String getCommand() {
        return command;
    }

    /**
     * Changes the command to execute.
     *
     * @param command the new command
     */
    public void setCommand(String command) {
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

    @Override
    public Player getPlayer() {
        return player;
    }
}
