package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.CancellableEvent;

/**
 * Called every time a player send a message starting by '/'
 */
public class PlayerCommandEvent extends CancellableEvent {

    private Player player;
    private String command;

    public PlayerCommandEvent(Player player, String command) {
        this.player = player;
        this.command = command;
    }

    /**
     * @return the player who want to execute the command
     */
    public Player getPlayer() {
        return player;
    }

    /**
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
