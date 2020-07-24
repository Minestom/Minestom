package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;

/**
 * Called after a chunk being unload to a certain player
 * could be used to unload the chunk internally in order to save memory
 */
public class PlayerChunkUnloadEvent extends Event {

    private final Player player;
    private final int chunkX, chunkZ;

    public PlayerChunkUnloadEvent(Player player, int chunkX, int chunkZ) {
        this.player = player;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    /**
     * Get the player
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the chunk X
     *
     * @return the chunk X
     */
    public int getChunkX() {
        return chunkX;
    }

    /**
     * Get the chunk Z
     *
     * @return the chunk Z
     */
    public int getChunkZ() {
        return chunkZ;
    }
}
