package net.minestom.server.event.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;
import net.minestom.server.instance.Instance;

/**
 * Called when a new instance is set for an entity
 */
public class EntitySpawnEvent extends Event {

    private final Entity entity;
    private final Instance spawnInstance;

    public EntitySpawnEvent(Entity entity, Instance spawnInstance) {
        this.entity = entity;
        this.spawnInstance = spawnInstance;
    }

    /**
     * Get the entity who spawned in the instance
     *
     * @return the entity
     */
    public Entity getEntity() {
        return entity;
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
