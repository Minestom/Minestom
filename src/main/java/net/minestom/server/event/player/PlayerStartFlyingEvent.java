package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.event.trait.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player start flying.
 */
public class PlayerStartFlyingEvent implements PlayerEvent, EntityInstanceEvent {

    private final Player player;

    public PlayerStartFlyingEvent(@NotNull Player player) {
        this.player = player;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }
}
