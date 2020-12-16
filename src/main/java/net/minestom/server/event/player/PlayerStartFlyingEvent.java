package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player start flying.
 */
public class PlayerStartFlyingEvent extends PlayerEvent {

    public PlayerStartFlyingEvent(@NotNull Player player) {
        super(player);
    }
}
