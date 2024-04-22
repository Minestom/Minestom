package net.minestom.server.event.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.EntityInstanceEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called right before an entity is removed
 */
public class EntityDespawnEvent implements EntityInstanceEvent {

    private final Entity entity;

    public EntityDespawnEvent(@NotNull Entity entity) {
        this.entity = entity;
    }

    /**
     * Gets the entity who is about to be removed
     *
     * @return the entity
     */
    @NotNull
    @Override
    public Entity getEntity() {
        return entity;
    }
}
