package net.minestom.server.event.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a new instance is set for an entity.
 */
public record EntitySpawnEvent(@NotNull Entity entity, @NotNull Instance spawnInstance) implements EntityInstanceEvent {

    /**
     * Gets the entity who spawned in the instance.
     *
     * @return the entity
     */
    @NotNull
    @Override
    public Entity entity() {
        return entity;
    }

    /**
     * Gets the entity new instance.
     *
     * @return the instance
     */
    @Override
    @NotNull
    public Instance spawnInstance() {
        return spawnInstance;
    }

}
