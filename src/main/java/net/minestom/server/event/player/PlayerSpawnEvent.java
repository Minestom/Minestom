package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a new instance is set for a player.
 */
public record PlayerSpawnEvent(@NotNull Player player, @NotNull Instance spawnInstance, boolean firstSpawn) implements PlayerInstanceEvent {
    /**
     * Use {@link #instance()} instead.
     * <p>
     * Gets the player's new instance.
     *
     * @return the instance
     */
    @Override
    @Deprecated
    public @NotNull Instance spawnInstance() {
        return spawnInstance;
    }

    /**
     * 'true' if the player is spawning for the first time. 'false' if this spawn event was triggered by a dimension teleport
     *
     * @return true if this is the first spawn, false otherwise
     */
    @Override
    public boolean firstSpawn() {
        return firstSpawn;
    }
}
