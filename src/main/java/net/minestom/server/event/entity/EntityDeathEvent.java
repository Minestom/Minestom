package net.minestom.server.event.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.EntityEvent;
import org.jetbrains.annotations.NotNull;

public class EntityDeathEvent implements EntityEvent {

    // TODO cause
    private final Entity entity;

    public EntityDeathEvent(@NotNull Entity entity) {
        this.entity = entity;
    }

    @Override
    public @NotNull Entity getEntity() {
        return entity;
    }
}
