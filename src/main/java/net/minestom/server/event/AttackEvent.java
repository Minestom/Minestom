package net.minestom.server.event;

import net.minestom.server.entity.Entity;

public class AttackEvent extends Event {

    private Entity target;

    public AttackEvent(Entity target) {
        this.target = target;
    }

    public Entity getTarget() {
        return target;
    }
}
