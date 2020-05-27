package net.minestom.server.entity.damage;

import net.minestom.server.entity.Entity;

/**
 * Represents damage inflicted by an entity
 */
public class EntityDamage extends DamageType {

    private final Entity source;

    public EntityDamage(Entity source) {
        super("entity_source");
        this.source = source;
    }

    /**
     * @return the source of the damage
     */
    public Entity getSource() {
        return source;
    }
}
