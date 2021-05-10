package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player starts sprinting.
 */
public class PlayerStartSprintingEvent extends PlayerEvent {

    public PlayerStartSprintingEvent(@NotNull Player player) {
        super(player);
    }
}
