package net.minestom.server.event.player;

import net.kyori.adventure.resource.ResourcePackStatus;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player warns the server of a resource pack status.
 */
public record PlayerResourcePackStatusEvent(@NotNull Player player, @NotNull ResourcePackStatus status) implements PlayerEvent {

    /**
     * Gets the resource pack status.
     *
     * @return the resource pack status
     */
    @Override
    public @NotNull ResourcePackStatus status() {
        return status;
    }
}
