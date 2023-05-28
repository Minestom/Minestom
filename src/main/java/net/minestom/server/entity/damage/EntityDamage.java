package net.minestom.server.entity.damage;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

/**
 * Represents damage inflicted by an {@link Entity}.
 */
public class EntityDamage extends Damage {

    private final Entity source;

    public EntityDamage(@NotNull Entity source, float amount) {
        super(DamageType.MOB_ATTACK, amount);
        this.source = source;
    }

    /**
     * Gets the source of the damage.
     *
     * @return the source
     */
    @NotNull
    public Entity getSource() {
        return source;
    }
}
