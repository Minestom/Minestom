package fr.themode.minestom.event;

import fr.themode.minestom.entity.Entity;

public class InteractEvent extends Event {

    private Entity target;

    public InteractEvent(Entity target) {
        this.target = target;
    }

    public Entity getTarget() {
        return target;
    }
}