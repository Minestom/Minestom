package net.minestom.server.event.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;

public class AttackEvent extends Event {

    private Entity target;

    public AttackEvent(Entity target) {
        this.target = target;
    }

    public Entity getTarget() {
        return target;
    }
}
