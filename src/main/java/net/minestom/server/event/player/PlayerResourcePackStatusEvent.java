package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.resourcepack.ResourcePackStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player warns the server of a resource pack status.
 */
public class PlayerResourcePackStatusEvent implements PlayerEvent {

    private final Player player;
    private final ResourcePackStatus status;

    public PlayerResourcePackStatusEvent(@NotNull Player player, @NotNull ResourcePackStatus status) {
        this.player = player;
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

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }
}
