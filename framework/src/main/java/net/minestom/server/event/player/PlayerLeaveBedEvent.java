package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;

public class PlayerLeaveBedEvent implements CancellableEvent, PlayerInstanceEvent {

    private final Player player;
    private boolean cancelled = false;

    public PlayerLeaveBedEvent(Player player) {
        this.player = player;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

}
