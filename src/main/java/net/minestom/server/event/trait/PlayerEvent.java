package net.minestom.server.event.trait;

import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;

public interface PlayerEvent extends Event {

    /**
     * Gets the player.
     *
     * @return the player
     */
    @NotNull Player getPlayer();
}
