package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player start flying.
 */
public class PlayerStartFlyingEvent extends Event {

    private final Player player;

    public PlayerStartFlyingEvent(@NotNull Player player) {
        this.player = player;
    }

    /**
     * Gets the player who started flying.
     *
     * @return the player
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

}
