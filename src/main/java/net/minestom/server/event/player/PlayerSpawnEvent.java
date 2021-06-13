package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.PlayerEvent;
import net.minestom.server.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a new World is set for a player.
 */
public class PlayerSpawnEvent extends PlayerEvent {

    private final World spawnWorld;
    private final boolean firstSpawn;

    public PlayerSpawnEvent(@NotNull Player player, @NotNull World spawnWorld, boolean firstSpawn) {
        super(player);
        this.spawnWorld = spawnWorld;
        this.firstSpawn = firstSpawn;
    }

    /**
     * Gets the entity's new World.
     *
     * @return the World
     */
    @NotNull
    public World getSpawnWorld() {
        return spawnWorld;
    }

    /**
     * 'true' if the player is spawning for the first time. 'false' if this spawn event was triggered by a dimension teleport
     *
     * @return true if this is the first spawn, false otherwise
     */
    public boolean isFirstSpawn() {
        return firstSpawn;
    }
}
