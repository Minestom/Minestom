package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerInstanceEvent;

public class PlayerStopFlyingWithElytraEvent implements PlayerInstanceEvent {

    private final Player player;

    public PlayerStopFlyingWithElytraEvent(Player player) {
        this.player = player;
    }

    @Override
    public Player getPlayer() {
        return player;
    }
}
