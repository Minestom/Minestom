package net.minestom.server.event.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.EntityEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player does a left click on an entity or with
 * {@link net.minestom.server.entity.EntityCreature#attack(Entity)}.
 */
public class EntityAttackEvent extends EntityEvent {

    private final Entity target;

    public EntityAttackEvent(@NotNull Entity source, @NotNull Entity target) {
        super(source);
        this.target = target;
    }

    /**
     * @return the target of the attack
     */
    @NotNull
    public Entity getTarget() {
        return target;
    }
}
