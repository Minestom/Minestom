package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player stops sneaking.
 */
public class PlayerStopSneakingEvent extends PlayerEvent {

    public PlayerStopSneakingEvent(@NotNull Player player) {
        super(player);
    }
}
