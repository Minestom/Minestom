package net.minestom.server.event.entity;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.CancellableEvent;

/**
 * Called with {@link LivingEntity#damage(DamageType, float)}.
 */
public class EntityDamageEvent extends CancellableEvent {

    private final LivingEntity entity;
    private final DamageType damageType;
    private float damage;

    public EntityDamageEvent(LivingEntity entity, DamageType damageType, float damage) {
        this.entity = entity;
        this.damageType = damageType;
        this.damage = damage;
    }

    /**
     * Gets the damaged entity.
     *
     * @return the damaged entity
     */
    public LivingEntity getEntity() {
        return entity;
    }

    /**
     * Gets the damage type.
     *
     * @return the damage type
     */
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
