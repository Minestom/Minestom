package net.minestom.server.event.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;

public class EntityAttackEvent extends Event {

    private Entity source;
    private Entity target;

    public EntityAttackEvent(Entity source, Entity target) {
        this.source = source;
        this.target = target;
    }

    public Entity getSource() {
        return source;
    }

    public Entity getTarget() {
        return target;
    }
}
