package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerInstanceEvent;

/**
 * @deprecated Use {@link PlayerInputEvent} instead.
 */
@Deprecated(forRemoval = true)
public class PlayerStopSneakingEvent implements PlayerInstanceEvent {

    private final Player player;

    public PlayerStopSneakingEvent(Player player) {
        this.player = player;
    }

    @Override
    public Player getPlayer() {
        return player;
    }
}
