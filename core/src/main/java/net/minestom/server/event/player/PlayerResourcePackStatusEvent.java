package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.PlayerEvent;
import net.minestom.server.resourcepack.ResourcePackStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player warns the server of a resource pack status.
 */
public class PlayerResourcePackStatusEvent extends PlayerEvent {

    private final ResourcePackStatus status;

    public PlayerResourcePackStatusEvent(@NotNull Player player, @NotNull ResourcePackStatus status) {
        super(player);
        this.status = status;
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
