package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.CancellableEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called every time a player send a message starting by '/'.
 */
public class PlayerCommandEvent extends CancellableEvent {

    private final Player player;
    private String command;

    public PlayerCommandEvent(@NotNull Player player, @NotNull String command) {
        this.player = player;
        this.command = command;
    }

    /**
     * Gets the player who sent the command.
     *
     * @return the player
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the command used (command name + arguments).
     *
     * @return the command that the player wants to execute
     */
    @NotNull
    public String getCommand() {
        return command;
    }

    /**
     * Changes the command to execute.
     *
     * @param command the new command
     */
    public void setCommand(@NotNull String command) {
        this.command = command;
    }
}
