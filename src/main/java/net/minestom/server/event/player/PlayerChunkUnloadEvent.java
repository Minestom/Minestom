package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a chunk "unload" is being sent to a player.
 * <p>
 * This event can be called on any thread.
 * This event will not be called concurrently with {@link PlayerChunkLoadEvent}, so tracking
 * chunks visible to the player is possible with both events.
 */
public class PlayerChunkUnloadEvent implements PlayerInstanceEvent {

    private final Player player;
    private final int chunkX, chunkZ;

    public PlayerChunkUnloadEvent(@NotNull Player player, int chunkX, int chunkZ) {
        this.player = player;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    /**
     * Gets the chunk X.
     *
     * @return the chunk X
     */
    public int getChunkX() {
        return chunkX;
    }

    /**
     * Gets the chunk Z.
     *
     * @return the chunk Z
     */
    public int getChunkZ() {
        return chunkZ;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }
}
