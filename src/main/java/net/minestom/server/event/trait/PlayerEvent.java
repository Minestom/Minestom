package net.minestom.server.event.trait;

import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface PlayerEvent {

    /**
     * Gets the player.
     *
     * @return the player
     */
    @NotNull Player getPlayer();
}
