package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player stops sprinting.
 */
public class PlayerStopSprintingEvent extends PlayerEvent {

    public PlayerStopSprintingEvent(@NotNull Player player) {
        super(player);
    }
}
