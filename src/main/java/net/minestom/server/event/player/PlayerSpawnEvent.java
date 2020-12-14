package net.minestom.server.event.player;

import org.jetbrains.annotations.NotNull;

import net.minestom.server.entity.Player;
import net.minestom.server.event.entity.EntitySpawnEvent;
import net.minestom.server.instance.Instance;

/**
 * Called when a new instance is set for a player.
 */
public class PlayerSpawnEvent extends EntitySpawnEvent {

    private final boolean firstSpawn;

    public PlayerSpawnEvent(@NotNull Player player, @NotNull Instance spawnInstance, boolean firstSpawn) {
        super(player, spawnInstance);
        this.firstSpawn = firstSpawn;
    }

    /**
     * Gets the player who spawned.
     * <p>
     * Shortcut for casting {@link #getEntity()}.
     *
     * @return
     */
    @NotNull
    public Player getPlayer() {
        return (Player) getEntity();
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
