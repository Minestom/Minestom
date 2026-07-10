package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerInstanceEvent;

/**
 * Called when a player disconnect.
 */
public class PlayerDisconnectEvent implements PlayerInstanceEvent {

    private final Player player;

    public PlayerDisconnectEvent(Player player) {
        this.player = player;
    }

    @Override
    public Player getPlayer() {
        return player;
    }
}
