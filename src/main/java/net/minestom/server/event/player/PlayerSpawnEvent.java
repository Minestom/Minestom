package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.instance.Instance;

/**
 * Called when a new instance is set for a player.
 */
public class PlayerSpawnEvent implements PlayerInstanceEvent {
    private final Player player;
    private final Instance spawnInstance;
    private final boolean firstSpawn;

    public PlayerSpawnEvent(Player player, Instance spawnInstance, boolean firstSpawn) {
        this.player = player;
        this.spawnInstance = spawnInstance;
        this.firstSpawn = firstSpawn;
    }

    /**
     * Gets the player's new instance.
     *
     * @return the instance
     */
    @Deprecated
    public Instance getSpawnInstance() {
        return spawnInstance;
    }

    /**
     * 'true' if the player is spawning for the first time. 'false' if this spawn event was triggered by a dimension teleport
     *
     * @return true if this is the first spawn, false otherwise
     */
    public boolean isFirstSpawn() {
        return firstSpawn;
    }

    @Override
    public Player getPlayer() {
        return player;
    }
}
