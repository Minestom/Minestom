package net.minestom.server.event.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.EntityEvent;
import net.minestom.server.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a new World is set for an entity.
 */
public class EntitySpawnEvent extends EntityEvent {

    private final World spawnWorld;

    public EntitySpawnEvent(@NotNull Entity entity, @NotNull World spawnWorld) {
        super(entity);
        this.spawnWorld = spawnWorld;
    }

    /**
     * Gets the entity who spawned in the World.
     *
     * @return the entity
     */
    @NotNull
    @Override
    public Entity getEntity() {
        return entity;
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

}
