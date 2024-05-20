package net.minestom.server.event.entity;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a living entity swings its hand.
 *
 * @see LivingEntity#swingHand(LivingEntity.Hand)
 */
public final class EntitySwingHandEvent implements EntityInstanceEvent, CancellableEvent {

    private final LivingEntity entity;
    private final LivingEntity.Hand hand;

    private boolean cancelled;

    /**
     * Constructs an {@link EntitySwingHandEvent}.
     *
     * @param entity an entity that swung the hand
     * @param hand a type of hand that was swung
     */
    public EntitySwingHandEvent(@NotNull LivingEntity entity, LivingEntity.@NotNull Hand hand) {
        this.entity = entity;
        this.hand = hand;
    }

    @Override
    public @NotNull LivingEntity getEntity() {
        return entity;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    /**'
     * Gets a type of hand that was swung.
     *
     * @return the type
     */
    public LivingEntity.@NotNull Hand getHand() {
        return hand;
    }
}