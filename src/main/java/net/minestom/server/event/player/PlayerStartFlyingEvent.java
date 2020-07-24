package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;

/**
 * Called when a player start flying
 */
public class PlayerStartFlyingEvent extends Event {

    private final Player player;

    public PlayerStartFlyingEvent(Player player) {
        this.player = player;
    }

    /**
     * Get the player who started flying
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

}
