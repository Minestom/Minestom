package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player stops sneaking.
 */
public class PlayerStopSneakingEvent implements PlayerInstanceEvent {

    private final Player player;

    public PlayerStopSneakingEvent(@NotNull Player player) {
        this.player = player;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }
}
