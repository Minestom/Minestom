package net.minestom.server.entity.ai;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents an entity which can contain
 * {@link Goal goal selectors} and {@link TargetSelector target selectors}.
 * <p>
 * Both types of selectors are being stored in {@link GoalSelector AI groups}.
 * For every group there could be only a single {@link Goal goal selector} running at a time,
 * but multiple groups are independent of each other, so each of them can have own goal selector running.
 */
public interface EntityAI {

    /**
     * Gets the goal selector of this entity. This can be used to add AI goals.
     *
     * @return the goal selector
     */
    GoalSelector getGoalSelector();

    /**
     * Returns a modifiable list of target selectors for this entity.
     * <p>
     * The order of this list determines priority (with the first selector being higher priority than the next, and so on).
     *
     * @return a modifiable list of target selectors
     */
    List<TargetSelector> getTargetSelectors();

    /**
     * Gets the entity target.
     *
     * @return the entity target, can be null if not any
     */
    @Nullable Entity getTarget();

    /**
     * Changes the entity target.
     *
     * @param target the new entity target, null to remove
     */
    void setTarget(@Nullable Entity target);

    /**
     * Tries to find a target using the target selectors from {@link EntityAI#getTargetSelectors()}.
     * The target returned by {@link EntityAI#getTarget()} will be updated.
     *
     * @return the new target, null if none was found
     */
    default @Nullable Entity findTarget() {
        for (TargetSelector targetSelector : getTargetSelectors()) {
            final Entity entity = targetSelector.findTarget();
            if (entity != null) {
                setTarget(entity);
                return entity;
            }
        }
        return null;
    }
}
