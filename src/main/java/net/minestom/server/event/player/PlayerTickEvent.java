package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerInstanceEvent;

/**
 * Called at each player tick.
 */
public class PlayerTickEvent implements PlayerInstanceEvent {

    private final Player player;

    public PlayerTickEvent(Player player) {
        this.player = player;
    }

    @Override
    public Player getPlayer() {
        return player;
    }
}
