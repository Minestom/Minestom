package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called after a chunk being unload to a certain player.
 * <p>
 * Could be used to unload the chunk internally in order to save memory.
 */
public class PlayerChunkUnloadEvent extends PlayerEvent {

    private final int chunkX, chunkZ;

    public PlayerChunkUnloadEvent(@NotNull Player player, int chunkX, int chunkZ) {
        super(player);
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
}
