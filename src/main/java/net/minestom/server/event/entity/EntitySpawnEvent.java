package net.minestom.server.event.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.instance.Instance;

/**
 * Called when a new instance is set for an entity.
 */
public class EntitySpawnEvent implements EntityInstanceEvent {

    private final Entity entity;
    private final Instance spawnInstance;

    public EntitySpawnEvent(Entity entity, Instance spawnInstance) {
        this.entity = entity;
        this.spawnInstance = spawnInstance;
    }

    /**
     * Gets the entity who spawned in the instance.
     *
     * @return the entity
     */
    @Override
    public Entity getEntity() {
        return entity;
    }

    /**
     * Gets the entity new instance.
     *
     * @return the instance
     */
    public Instance getSpawnInstance() {
        return spawnInstance;
    }

}
