package net.minestom.server.entity.damage;

import net.minestom.server.entity.Entity;

/**
 * Represents damage inflicted by an {@link Entity}.
 */
public class EntityDamage extends DamageType {

    private final Entity source;

    public EntityDamage(Entity source) {
        super("entity_source");
        this.source = source;
    }

    /**
     * Gets the source of the damage.
     *
     * @return the source
     */
    public Entity getSource() {
        return source;
    }
}
