package net.minestom.server.event.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.EntityEvent;
import org.jetbrains.annotations.NotNull;

public class EntityDeathEvent extends EntityEvent {

    private final DamageType damageType;

    public EntityDeathEvent(@NotNull Entity entity, @NotNull DamageType damageType) {
        super(entity);

        this.damageType = damageType;
    }

    public DamageType getDamageType() {
        return damageType;
    }
}
