package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;

/**
 * Called when a player stop flying
 */
public class PlayerStopFlyingEvent extends Event {

    private final Player player;

    public PlayerStopFlyingEvent(Player player) {
        this.player = player;
    }

    /**
     * Get the player who stopped flying
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }
}
