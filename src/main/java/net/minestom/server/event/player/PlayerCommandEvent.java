package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.CancellableEvent;

/**
 * Called every time a player send a message starting by '/'
 */
public class PlayerCommandEvent extends CancellableEvent {

    private final Player player;
    private String command;

    public PlayerCommandEvent(Player player, String command) {
        this.player = player;
        this.command = command;
    }

    /**
     * Get the player who sent the command
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the command used (command name + arguments)
     *
     * @return the command that the player wants to execute
     */
    public String getCommand() {
        return command;
    }

    /**
     * Change the command to execute
     *
     * @param command the new command
     */
    public void setCommand(String command) {
        this.command = command;
    }
}
