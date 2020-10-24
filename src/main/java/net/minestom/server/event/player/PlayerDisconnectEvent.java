package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player disconnect.
 */
public class PlayerDisconnectEvent extends Event {

    private final Player player;

    public PlayerDisconnectEvent(@NotNull Player player) {
        this.player = player;
    }

    /**
     * Gets the player who is disconnecting.
     *
     * @return the player
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }
}
