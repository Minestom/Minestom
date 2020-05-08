package net.minestom.server.event.player;

import net.minestom.server.event.entity.EntitySpawnEvent;
import net.minestom.server.instance.Instance;

public class PlayerSpawnEvent extends EntitySpawnEvent {
    private final boolean firstSpawn;

    public PlayerSpawnEvent(Instance spawnInstance, boolean firstSpawn) {
        super(spawnInstance);
        this.firstSpawn = firstSpawn;
    }

    /**
     * 'true' if the player is spawning for the first time. 'false' if this spawn event was triggered by a dimension teleport
     * @return
     */
    public boolean isFirstSpawn() {
        return firstSpawn;
    }
}
