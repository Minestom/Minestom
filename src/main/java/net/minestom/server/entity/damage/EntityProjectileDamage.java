package net.minestom.server.entity.damage;

import net.minestom.server.entity.Entity;

/**
 * Represents damage inflicted by an entity, via a projectile
 */
public class EntityProjectileDamage extends DamageType {

    private Entity shooter;
    private final Entity projectile;

    public EntityProjectileDamage(Entity shooter, Entity projectile) {
        super("projectile_source");
        this.shooter = shooter;
        this.projectile = projectile;
    }

    public Entity getProjectile() {
        return projectile;
    }

    public Entity getShooter() {
        return shooter;
    }
}
