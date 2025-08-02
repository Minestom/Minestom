package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerInstanceEvent;

/**
 * Called when a player receive a new chunk data.
 */
public class PlayerChunkLoadEvent implements PlayerInstanceEvent {

    private final Player player;
    private final int chunkX, chunkZ;

    public PlayerChunkLoadEvent(Player player, int chunkX, int chunkZ) {
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
    public Player getPlayer() {
        return player;
    }
}
