package net.minestom.server.entity.ai.target;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.function.Predicate;

/**
 * Target the closest targetable entity (based on the target predicate)
 */
public class ClosestEntityTarget implements TargetSelector {

    private final double range;
    private final Predicate<Entity> targetPredicate;

    /**
     * @param range           the maximum range the entity can target others within
     * @param targetPredicate the predicate used to check if the other entity can be targeted
     */
    public ClosestEntityTarget(double range,
                               @NotNull Predicate<Entity> targetPredicate) {
        this.range = range;
        this.targetPredicate = targetPredicate;
    }

    @Override
    public Entity findTargetEntity(@NotNull EntityCreature entityCreature) {

        Instance instance = entityCreature.getInstance();

        if (instance == null) {
            return null;
        }

        return instance.getNearbyEntities(entityCreature.getPosition(), range).stream()
                // Don't target our self and make sure entity is valid
                .filter(ent -> !entityCreature.equals(ent) && !ent.isRemoved())
                .filter(targetPredicate)
                .min(Comparator.comparingDouble(e -> e.getDistanceSquared(entityCreature)))
                .orElse(null);

    }

    @Override
    public @Nullable Pos findTargetPosition(@NotNull EntityCreature entityCreature) {
        return null;
    }
}
