package net.minestom.server.entity.ai.target;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.function.Predicate;

/**
 * Target the closest targetable entity (based on the target predicate)
 */
public class ClosestEntityTarget extends TargetSelector {

    private final double range;
    private final Predicate<Entity> targetPredicate;

    /**
     * @param entityCreature the entity (self)
     * @param range          the maximum range the entity can target others within
     * @param entitiesTarget the entities to target by class
     * @deprecated Use {@link #ClosestEntityTarget(EntityCreature, double, Predicate)}
     */
    @SafeVarargs
    @Deprecated
    public ClosestEntityTarget(@NotNull EntityCreature entityCreature, float range,
                               @NotNull Class<? extends LivingEntity>... entitiesTarget) {
        this(entityCreature, range, ent -> {
            Class<? extends Entity> clazz = ent.getClass();
            for (Class<? extends LivingEntity> targetClass : entitiesTarget) {
                if (targetClass.isAssignableFrom(clazz)) {
                    return true;
                }
            }
            return false;
        });
    }

    /**
     * @param entityCreature  the entity (self)
     * @param range           the maximum range the entity can target others within
     * @param targetPredicate the predicate used to check if the other entity can be targeted
     */
    public ClosestEntityTarget(@NotNull EntityCreature entityCreature, double range,
                               @NotNull Predicate<Entity> targetPredicate) {
        super(entityCreature);
        this.range = range;
        this.targetPredicate = targetPredicate;
    }

    @Override
    public Entity findTarget() {

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

}
