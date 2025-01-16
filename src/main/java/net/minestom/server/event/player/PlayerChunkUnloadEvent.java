package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called after a chunk being unload to a certain player.
 * <p>
 * Could be used to unload the chunk internally in order to save memory.
 */
public record PlayerChunkUnloadEvent(@NotNull Player player, int chunkX, int chunkZ) implements PlayerInstanceEvent {

    /**
     * Gets the chunk X.
     *
     * @return the chunk X
     */
    @Override
    public int chunkX() {
        return chunkX;
    }

    /**
     * Gets the chunk Z.
     *
     * @return the chunk Z
     */
    @Override
    public int chunkZ() {
        return chunkZ;
    }
}
