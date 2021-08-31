package net.minestom.server.event.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called with {@link LivingEntity#damage(DamageType, float)}.
 */
public class EntityDamageEvent implements EntityEvent, CancellableEvent {

    private final Entity entity;
    private final DamageType damageType;
    private float damage;

    private boolean cancelled;

    public EntityDamageEvent(@NotNull Entity entity, @NotNull DamageType damageType, float damage) {
        this.entity = entity;
        this.damageType = damageType;
        this.damage = damage;
    }

    @NotNull
    @Override
    public Entity getEntity() {
        return entity;
    }

    /**
     * Gets the damage type.
     *
     * @return the damage type
     */
    @NotNull
    public DamageType getDamageType() {
        return damageType;
    }

    /**
     * Gets the damage amount.
     *
     * @return the damage amount
     */
    public float getDamage() {
        return damage;
    }

    /**
     * Changes the damage amount.
     *
     * @param damage the new damage amount
     */
    public void setDamage(float damage) {
        this.damage = damage;
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
