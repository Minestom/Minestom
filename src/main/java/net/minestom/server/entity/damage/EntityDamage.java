package net.minestom.server.entity.damage;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Represents damage inflicted by an {@link Entity}.
 */
public class EntityDamage extends Damage {
    protected EntityDamage(@NotNull DamageType type, @Nullable Entity source, @Nullable Entity attacker, float amount) {
        super(type, source, attacker, null, amount);
    }

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
        return Objects.requireNonNull(super.getSource());
    }

    @Override
    public @NotNull Entity getAttacker() {
        return getSource();
    }
}