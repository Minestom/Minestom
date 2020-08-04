package net.minestom.server.event.entity;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.CancellableEvent;

/**
 * Called with {@link net.minestom.server.entity.LivingEntity#damage(DamageType, float)}
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
     * Get the damaged entity
     *
     * @return the damaged entity
     */
    public LivingEntity getEntity() {
        return entity;
    }

    /**
     * Get the damage type
     *
     * @return the damage type
     */
    public DamageType getDamageType() {
        return damageType;
    }

    /**
     * Get the damage amount
     *
     * @return the damage amount
     */
    public float getDamage() {
        return damage;
    }

    /**
     * Change the damage amount
     *
     * @param damage the new damage amount
     */
    public void setDamage(float damage) {
        this.damage = damage;
    }
}
