package net.minestom.server.event.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;

/**
 * Called when a player does a left click on an entity or with
 * {@link net.minestom.server.entity.EntityCreature#attack(Entity)}
 */
public class EntityAttackEvent extends Event {

    private final Entity source;
    private final Entity target;

    public EntityAttackEvent(Entity source, Entity target) {
        this.source = source;
        this.target = target;
    }

    /**
     * @return the source of the attack
     */
    public Entity getSource() {
        return source;
    }

    /**
     * @return the target of the attack
     */
    public Entity getTarget() {
        return target;
    }
}
