package net.minestom.server.event.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.potion.Potion;

public class EntityPotionAddEvent implements EntityInstanceEvent, CancellableEvent {

    private final Entity entity;
    private final Potion potion;

    private boolean cancelled = false;

    public EntityPotionAddEvent(Entity entity, Potion potion) {
        this.entity = entity;
        this.potion = potion;
    }


    @Override
    public Entity getEntity() {
        return entity;
    }

    /**
     * Returns the potion that was added.
     *
     * @return the added potion.
     */
    public Potion getPotion() {
        return potion;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
