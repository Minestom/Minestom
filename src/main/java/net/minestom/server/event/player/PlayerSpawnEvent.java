package net.minestom.server.event.player;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.entity.EntitySpawnEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a new instance is set for a player.
 */
public class PlayerSpawnEvent extends EntitySpawnEvent {

    private final boolean firstSpawn;

    public PlayerSpawnEvent(@NotNull Entity entity, @NotNull Instance spawnInstance, boolean firstSpawn) {
        super(entity, spawnInstance);
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
