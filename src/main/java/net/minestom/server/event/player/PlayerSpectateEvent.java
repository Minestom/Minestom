package net.minestom.server.event.player;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called by the SpectateListener when a player starts spectating an entity.
 */
@SuppressWarnings("ClassCanBeRecord")
public class PlayerSpectateEvent implements PlayerEvent {
    private final Player player;
    private final Entity target;

    public PlayerSpectateEvent(Player player, Entity target) {
        this.player = player;
        this.target = target;
    }

    public Entity getTarget() {
        return target;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }
}
