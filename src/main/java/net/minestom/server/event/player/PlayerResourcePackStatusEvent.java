package net.minestom.server.event.player;

import net.kyori.adventure.resource.ResourcePackStatus;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;

/**
 * Called when a player warns the server of a resource pack status.
 */
public class PlayerResourcePackStatusEvent implements PlayerEvent {

    private final Player player;
    private final ResourcePackStatus status;

    public PlayerResourcePackStatusEvent(Player player, ResourcePackStatus status) {
        this.player = player;
        this.status = status;
    }

    /**
     * Gets the resource pack status.
     *
     * @return the resource pack status
     */
    public ResourcePackStatus getStatus() {
        return status;
    }

    @Override
    public Player getPlayer() {
        return player;
    }
}
