package net.minestom.server.event.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;

public class EntityDeathEvent extends Event {

    private Entity entity;
    // TODO cause

    public EntityDeathEvent(Entity entity) {
        this.entity = entity;
    }

    /**
     * @return the entity that died
     */
    public Entity getEntity() {
        return entity;
    }
}
