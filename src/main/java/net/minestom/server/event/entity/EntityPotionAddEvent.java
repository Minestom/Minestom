package net.minestom.server.event.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.event.EntityEvent;
import net.minestom.server.potion.Potion;
import org.jetbrains.annotations.NotNull;

public class EntityPotionAddEvent extends EntityEvent implements CancellableEvent {

    private final Potion potion;

    private boolean cancelled;

    public EntityPotionAddEvent(@NotNull Entity entity, @NotNull Potion potion) {
        super(entity);
        this.potion = potion;
    }

    /**
     * Returns the potion that was added.
     *
     * @return the added potion.
     */
    @NotNull
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
