package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player disconnect.
 */
public class PlayerDisconnectEvent extends PlayerEvent {

    public PlayerDisconnectEvent(@NotNull Player player) {
        super(player);
    }
}
