package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;

public class PlayerChunkLoadEvent extends Event {

    private Player player;
    private int chunkX, chunkZ;

    public PlayerChunkLoadEvent(Player player, int chunkX, int chunkZ) {
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
