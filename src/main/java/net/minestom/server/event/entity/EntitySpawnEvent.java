package net.minestom.server.event.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a new instance is set for an entity.
 */
public class EntitySpawnEvent implements EntityInstanceEvent {

    private final Entity entity;
    private final Instance spawnInstance;

    public EntitySpawnEvent(@NotNull Entity entity, @NotNull Instance spawnInstance) {
        this.entity = entity;
        this.spawnInstance = spawnInstance;
    }

    /**
     * Gets the entity who spawned in the instance.
     *
     * @return the entity
     */
    @NotNull
    @Override
    public Entity getEntity() {
        return entity;
    }

    /**
     * Gets the entity new instance.
     *
     * @return the instance
     */
    @NotNull
    public Instance getSpawnInstance() {
        return spawnInstance;
    }

}
