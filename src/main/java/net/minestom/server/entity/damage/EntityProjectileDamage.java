package net.minestom.server.entity.damage;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents damage inflicted by an entity, via a projectile.
 */
public class EntityProjectileDamage extends Damage {

    public EntityProjectileDamage(@Nullable Entity shooter, @NotNull Entity projectile, float amount) {
        super(DamageType.MOB_PROJECTILE, projectile, shooter, null, amount);
    }

    /**
     * Gets the projectile responsible for the damage.
     *
     * @return the projectile
     */
    @NotNull
    public Entity getProjectile() {
        return getSource();
    }

    /**
     * Gets the shooter of the projectile.
     *
     * @return the shooter of the projectile, null if not any
     */
    @Nullable
    public Entity getShooter() {
        return getAttacker();
    }

    @Override
    public @NotNull Entity getSource() {
        return super.getSource();
    }
}