package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * Called at each player tick.
 */
public class PlayerTickEvent extends Event {

    private final Player player;

    public PlayerTickEvent(@NotNull Player player) {
        this.player = player;
    }

    /**
     * Gets the player.
     *
     * @return the player
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }
}
