package net.minestom.server.event.entity;

import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.CancellableEvent;

public class EntityDamageEvent extends CancellableEvent {

    private DamageType damageType;
    private float damage;

    public EntityDamageEvent(DamageType damageType, float damage) {
        this.damageType = damageType;
        this.damage = damage;
    }

    public DamageType getDamageType() {
        return damageType;
    }

    public float getDamage() {
        return damage;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }
}
