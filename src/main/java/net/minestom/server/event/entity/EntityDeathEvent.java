package net.minestom.server.event.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.EntityEvent;
import org.jetbrains.annotations.NotNull;

public class EntityDeathEvent extends EntityEvent {

    // TODO cause

    public EntityDeathEvent(@NotNull Entity entity) {
        super(entity);
    }
}
