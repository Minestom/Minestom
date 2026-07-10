package net.minestom.server.entity.damage;

import net.minestom.server.coordinate.Point;
import net.minestom.server.registry.RegistryKey;

/**
 * Represents damage that is associated with a certain position.
 */
public class PositionalDamage extends Damage {

    public PositionalDamage(RegistryKey<DamageType> type, Point sourcePosition, float amount) {
        super(type, null, null, sourcePosition, amount);
    }

}