package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerInstanceEvent;

/**
 * Called when a player indicates that they have finished loading into the world.
 *
 * <p>This is driven by the client so should be considered as such.</p>
 */
public class PlayerLoadedEvent implements PlayerInstanceEvent {
    private final Player player;

    public PlayerLoadedEvent(Player player) {
        this.player = player;
    }

    @Override
    public Player getPlayer() {
        return player;
    }
}
