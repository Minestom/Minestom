package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.resourcepack.ResourcePackStatus;

/**
 * Called when a player warns the server of a resource pack status
 */
public class PlayerResourcePackStatusEvent extends Event {

    private final Player player;
    private final ResourcePackStatus status;

    public PlayerResourcePackStatusEvent(Player player, ResourcePackStatus status) {
        this.player = player;
        this.status = status;
    }

    /**
     * Get the player who send a resource pack status
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the resource pack status
     *
     * @return the resource pack status
     */
    public ResourcePackStatus getStatus() {
        return status;
    }
}
