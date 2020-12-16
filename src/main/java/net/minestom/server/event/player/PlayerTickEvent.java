package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called at each player tick.
 */
public class PlayerTickEvent extends PlayerEvent {

    public PlayerTickEvent(@NotNull Player player) {
        super(player);
    }
}
