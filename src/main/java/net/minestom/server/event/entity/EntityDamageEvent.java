package net.minestom.server.event.entity;

import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.CancellableEvent;

/**
 * Called with {@link net.minestom.server.entity.LivingEntity#damage(DamageType, float)}
 */
public class EntityDamageEvent extends CancellableEvent {

    private DamageType damageType;
    private float damage;

    public EntityDamageEvent(DamageType damageType, float damage) {
        this.damageType = damageType;
        this.damage = damage;
    }

    /**
     * @return the damage type
     */
    public DamageType getDamageType() {
        return damageType;
    }

    /**
     * @return the damage amount
     */
    public float getDamage() {
        return damage;
    }

    /**
     * @param damage the new damage amount
     */
    public void setDamage(float damage) {
        this.damage = damage;
    }
}
