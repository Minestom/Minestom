package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player stop flying.
 */
public class PlayerStopFlyingEvent extends Event {

    private final Player player;

    public PlayerStopFlyingEvent(@NotNull Player player) {
        this.player = player;
    }

    /**
     * Gets the player who stopped flying.
     *
     * @return the player
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }
}
