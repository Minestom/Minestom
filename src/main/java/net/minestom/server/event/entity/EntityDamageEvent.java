package net.minestom.server.event.entity;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.CancellableEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called with {@link LivingEntity#damage(DamageType, float)}.
 */
public class EntityDamageEvent extends CancellableEvent {

    private final LivingEntity entity;
    private final DamageType damageType;
    private float damage;

    public EntityDamageEvent(@NotNull LivingEntity entity, @NotNull DamageType damageType, float damage) {
        this.entity = entity;
        this.damageType = damageType;
        this.damage = damage;
    }

    /**
     * Gets the damaged entity.
     *
     * @return the damaged entity
     */
    @NotNull
    public LivingEntity getEntity() {
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
}
