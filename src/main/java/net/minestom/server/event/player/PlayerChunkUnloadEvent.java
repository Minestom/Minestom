package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;

/**
 * Called after a chunk being unload to a certain player
 * could be used to unload the chunk internally in order to save memory
 */
public class PlayerChunkUnloadEvent extends Event {

    private Player player;
    private int chunkX, chunkZ;

    public PlayerChunkUnloadEvent(Player player, int chunkX, int chunkZ) {
        this.player = player;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    public Player getPlayer() {
        return player;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }
}
