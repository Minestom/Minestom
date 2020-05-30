package net.minestom.server.event.entity;

import net.minestom.server.event.Event;
import net.minestom.server.instance.Instance;

/**
 * Called when a new instance is set for an entity
 */
public class EntitySpawnEvent extends Event {

    private Instance spawnInstance;

    public EntitySpawnEvent(Instance spawnInstance) {
        this.spawnInstance = spawnInstance;
    }

    /**
     * Get the entity new instance
     *
     * @return the instance
     */
    public Instance getSpawnInstance() {
        return spawnInstance;
    }

}
