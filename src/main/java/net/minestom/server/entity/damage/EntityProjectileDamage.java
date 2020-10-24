package net.minestom.server.entity.damage;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents damage inflicted by an entity, via a projectile.
 */
public class EntityProjectileDamage extends DamageType {

    private final Entity shooter;
    private final Entity projectile;

    public EntityProjectileDamage(@Nullable Entity shooter, @NotNull Entity projectile) {
        super("projectile_source");
        this.shooter = shooter;
        this.projectile = projectile;
    }

    /**
     * Gets the projectile responsible for the damage.
     *
     * @return the projectile
     */
    @NotNull
    public Entity getProjectile() {
        return projectile;
    }

    /**
     * Gets the shooter of the projectile.
     *
     * @return the shooter of the projectile, null if not any
     */
    @Nullable
    public Entity getShooter() {
        return shooter;
    }
}
