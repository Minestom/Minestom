package net.minestom.server.event.player;

import net.kyori.adventure.resource.ResourcePackStatus;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;

import java.util.UUID;

/**
 * Called when a player warns the server of a resource pack status.
 */
public class PlayerResourcePackStatusEvent implements PlayerEvent {

    private final Player player;
    private final ResourcePackStatus status;
    private final UUID packUUID;

    public PlayerResourcePackStatusEvent(Player player, UUID packUUID, ResourcePackStatus status) {
        this.player = player;
        this.status = status;
        this.packUUID = packUUID;
    }

    /**
     * Gets the resource pack status.
     *
     * @return the resource pack status
     */
    public ResourcePackStatus getStatus() {
        return status;
    }

    /**
     * Gets the associated pack UUID that has resolved on the client with the particular status
     * @return the UUID of the resource pack
     */
    public UUID getPackUUID() {
        return packUUID;
    }

    @Override
    public Player getPlayer() {
        return player;
    }
}
