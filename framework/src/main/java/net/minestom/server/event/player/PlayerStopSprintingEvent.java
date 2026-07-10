package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerInstanceEvent;

/**
 * Called when a player stops sprinting.
 */
public class PlayerStopSprintingEvent implements PlayerInstanceEvent {

    private final Player player;

    public PlayerStopSprintingEvent(Player player) {
        this.player = player;
    }

    @Override
    public Player getPlayer() {
        return player;
    }
}
