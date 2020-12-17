package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player stop flying.
 */
public class PlayerStopFlyingEvent extends PlayerEvent {

    public PlayerStopFlyingEvent(@NotNull Player player) {
        super(player);
    }
}
