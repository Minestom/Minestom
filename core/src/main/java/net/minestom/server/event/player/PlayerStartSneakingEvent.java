package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player starts sneaking.
 */
public class PlayerStartSneakingEvent extends PlayerEvent {

    public PlayerStartSneakingEvent(@NotNull Player player) {
        super(player);
    }
}
