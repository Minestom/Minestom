package net.minestom.server.entity.ai.target;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.damage.EntityDamage;
import org.jetbrains.annotations.NotNull;

/**
 * Targets the last damager of this entity.
 */
public class LastEntityDamagerTarget extends TargetSelector {

    private final float range;

    public LastEntityDamagerTarget(@NotNull EntityCreature entityCreature, float range) {
        super(entityCreature);
        this.range = range;
    }

    @Override
    public Entity findTarget() {
        final DamageType damageType = entityCreature.getLastDamageSource();
        if (!(damageType instanceof EntityDamage entityDamage)) {
            // No damager recorded, return null
            return null;
        }
        final Entity entity = entityDamage.getSource();
        if (entity.isRemoved()) {
            // Entity not valid
            return null;
        }
        // Check range
        return entityCreature.getDistance(entity) < range ? entity : null;
    }
}
