package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.instance.Chunk;

/**
 * Called after a chunk being unload to a certain player
 * could be used to unload the chunk internally in order to save memory
 */
public class PlayerChunkUnloadEvent extends Event {

    private Player player;
    private Chunk chunk;

    public PlayerChunkUnloadEvent(Player player, Chunk chunk) {
        this.player = player;
        this.chunk = chunk;
    }

    public Player getPlayer() {
        return player;
    }

    public Chunk getChunk() {
        return chunk;
    }
}
