package net.minestom.server.event;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class EntityEvent extends Event {

    protected final Entity entity;

    public EntityEvent(@NotNull Entity entity) {
        this.entity = entity;
    }

    /**
     * Gets the entity of this event.
     *
     * @return the entity
     */
    @NotNull
    public Entity getEntity() {
        return entity;
    }
}
