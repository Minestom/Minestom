package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;

public class PlayerStartFlyingEvent extends Event {

    private Player player;

    public PlayerStartFlyingEvent(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

}
