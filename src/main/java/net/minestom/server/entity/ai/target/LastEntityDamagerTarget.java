package net.minestom.server.entity.ai.target;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.EntityDamage;
import org.jetbrains.annotations.Nullable;

/**
 * Targets the last damager of this entity.
 */
public class LastEntityDamagerTarget extends TargetSelector {

    private final float range;
    private final int forgetTicks;

    private long lastUsedDamageTick;

    public LastEntityDamagerTarget(EntityCreature entityCreature, float range, int forgetTicks) {
        super(entityCreature);
        this.range = range;
        this.forgetTicks = forgetTicks;
    }

    @Override
    public boolean canUse() {
        if (entityCreature.getLastDamageSource() == null) return false;
        if (entityCreature.getLastDamageTick() == lastUsedDamageTick) return false; // Don't use again for the same damage

        final long ticksSinceDamage = entityCreature.getAliveTicks() - entityCreature.getLastDamageTick();
        return ticksSinceDamage < forgetTicks;
    }

    @Override
    public @Nullable Entity findTarget() {
        this.lastUsedDamageTick = entityCreature.getLastDamageTick();

        final Damage damage = entityCreature.getLastDamageSource();
        if (!(damage instanceof EntityDamage entityDamage)) {
            // No damager recorded, return null
            return null;
        }
        final Entity entity = entityDamage.getSource();
        if (entity.isRemoved()) {
            // Entity not valid
            return null;
        }
        // Check range
        return entityCreature.getDistanceSquared(entity) < range * range ? entity : null;
    }
}
