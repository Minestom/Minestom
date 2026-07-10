package net.minestom.server.entity.damage;

import net.minestom.server.entity.Entity;

/**
 * Represents damage inflicted by an {@link Entity}.
 */
public class EntityDamage extends Damage {

    public EntityDamage(Entity source, float amount) {
        super(DamageType.MOB_ATTACK, source, source, null, amount);
    }

    /**
     * Gets the source of the damage.
     *
     * @return the source
     */
    @Override
    public Entity getSource() {
        return super.getSource();
    }

    @Override
    public Entity getAttacker() {
        return getSource();
    }
}