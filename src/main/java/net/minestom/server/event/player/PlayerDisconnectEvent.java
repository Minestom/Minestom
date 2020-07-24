package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;

/**
 * Called when a player disconnect
 */
public class PlayerDisconnectEvent extends Event {

    private final Player player;

    public PlayerDisconnectEvent(Player player) {
        this.player = player;
    }

    /**
     * Get the player who is disconnecting
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }
}
