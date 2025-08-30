package net.minestom.server.entity.ai.target;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.TargetSelector;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.function.Predicate;

/**
 * Target the closest targetable entity (based on the target predicate)
 */
public class ClosestEntityTarget extends TargetSelector {
    private final double range;
    private final Predicate<Entity> targetPredicate;
    private final int randomInterval;

    /**
     * @param entityCreature  the entity (self)
     * @param range           the maximum range the entity can target others within
     * @param randomInterval  the interval at which this selector can be used. Setting it to N would mean there is a 1 in N chance per tick.
     * @param targetPredicate the predicate used to check if the other entity can be targeted
     */
    public ClosestEntityTarget(EntityCreature entityCreature, double range,
                               int randomInterval, Predicate<Entity> targetPredicate) {
        super(entityCreature);
        this.range = range;
        this.randomInterval = randomInterval;
        this.targetPredicate = targetPredicate;
    }

    @Override
    public boolean canUse() {
        if (randomInterval <= 0) return true;
        return entityCreature.getAi().getRandom().nextInt(randomInterval) == 0;
    }

    @Override
    public @Nullable Entity findTarget() {
        return entityCreature.getInstance().getNearbyEntities(entityCreature.getPosition(), range).stream()
                // Don't target ourselves and make sure entity is valid
                .filter(ent -> !entityCreature.equals(ent) && !ent.isRemoved())
                .filter(targetPredicate)
                .min(Comparator.comparingDouble(e -> e.getDistanceSquared(entityCreature)))
                .orElse(null);
    }
}
