package net.minestom.server.entity.damage;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.registry.DynamicRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents damage inflicted by an {@link Entity}.
 */
public class EntityDamage extends Damage {
    private static final DynamicRegistry<DamageType> DAMAGE_TYPES = MinecraftServer.getDamageTypeRegistry();

    public EntityDamage(@NotNull Entity source, float amount) {
        super(Objects.requireNonNull(DAMAGE_TYPES.get(DamageType.MOB_ATTACK)),
                source, source, null, amount);
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