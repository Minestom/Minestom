package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerInstanceEvent;

public class PlayerStartFlyingWithElytraEvent implements PlayerInstanceEvent {

    private final Player player;

    public PlayerStartFlyingWithElytraEvent(Player player) {
        this.player = player;
    }

    @Override
    public Player getPlayer() {
        return player;
    }
}
