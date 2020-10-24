package net.minestom.server.event.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;

public class EntityDeathEvent extends Event {

    private final Entity entity;
    // TODO cause

    public EntityDeathEvent(@NotNull Entity entity) {
        this.entity = entity;
    }

    /**
     * Get the killed entity,
     *
     * @return the entity that died
     */
    @NotNull
    public Entity getEntity() {
        return entity;
    }
}
