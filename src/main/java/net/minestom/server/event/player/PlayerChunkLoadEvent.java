package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player receive a new chunk data.
 */
public record PlayerChunkLoadEvent(@NotNull Player player, int chunkX, int chunkZ) implements PlayerInstanceEvent {

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

    @Override
    public @NotNull Player player() {
        return player;
    }
}
