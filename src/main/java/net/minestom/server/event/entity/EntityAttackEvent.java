package net.minestom.server.event.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.EntityInstanceEvent;

/**
 * Called when a player does a left click on an entity or with
 * {@link net.minestom.server.entity.EntityCreature#attack(Entity)}.
 */
public class EntityAttackEvent implements EntityInstanceEvent {

    private final Entity entity;
    private final Entity target;

    public EntityAttackEvent(Entity source, Entity target) {
        this.entity = source;
        this.target = target;
    }

    @Override
    public Entity getEntity() {
        return entity;
    }

    /**
     * @return the target of the attack
     */
    public Entity getTarget() {
        return target;
    }
}
