package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;

/**
 * Called at each player tick
 */
public class PlayerTickEvent extends Event {

    private final Player player;

    public PlayerTickEvent(Player player) {
        this.player = player;
    }

    /**
     * Get the player
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }
}
