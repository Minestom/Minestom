package net.minestom.server.entity.damage;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

/**
 * Represents damage inflicted by an {@link Entity}.
 */
public class EntityDamage extends Damage {

    public EntityDamage(@NotNull Entity source, float amount) {
        super(DamageType.MOB_ATTACK, source, source, null, amount);
    }

    /**
     * Gets the source of the damage.
     *
     * @return the source
     */
    @Override
    public @NotNull Entity getSource() {
        return super.getSource();
    }

    @Override
    public @NotNull Entity getAttacker() {
        return getSource();
    }
}