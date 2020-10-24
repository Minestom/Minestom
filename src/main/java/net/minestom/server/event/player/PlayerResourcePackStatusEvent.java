package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.resourcepack.ResourcePackStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player warns the server of a resource pack status.
 */
public class PlayerResourcePackStatusEvent extends Event {

    private final Player player;
    private final ResourcePackStatus status;

    public PlayerResourcePackStatusEvent(@NotNull Player player, @NotNull ResourcePackStatus status) {
        this.player = player;
        this.status = status;
    }

    /**
     * Gets the player who send a resource pack status.
     *
     * @return the player
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the resource pack status.
     *
     * @return the resource pack status
     */
    @NotNull
    public ResourcePackStatus getStatus() {
        return status;
    }
}
