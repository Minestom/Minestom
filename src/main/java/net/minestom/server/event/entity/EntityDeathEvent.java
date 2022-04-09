package net.minestom.server.event.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.EntityInstanceEvent;
import org.jetbrains.annotations.NotNull;

public class EntityDeathEvent implements EntityInstanceEvent {

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
