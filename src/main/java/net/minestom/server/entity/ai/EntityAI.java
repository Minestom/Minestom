package net.minestom.server.entity.ai;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Container for all entity AI from {@link EntityCreature}.
 * <p>
 * It contains a {@link GoalSelector} which determines the active AI goals,
 * and it tracks the entity target with a list of {@link TargetSelector}.
 *
 * @see EntityAI#addGoal(Goal, GoalSelector.Slot...)
 * @see EntityAI#addTargetSelector(TargetSelector)
 */
public class EntityAI {
    private final GoalSelector goalSelector = new GoalSelector();
    private final List<TargetSelector> targetSelectors = new ArrayList<>();

    private @Nullable Entity target;

    public void tick(long time) {
        goalSelector.tick(time);

        // Re-evaluate target selection
        for (TargetSelector targetSelector : getTargetSelectors()) {
            if (!targetSelector.canUse()) continue;
            final Entity entity = targetSelector.findTarget();
            if (entity != null) {
                this.target = entity;
                break;
            }
        }
    }

    /**
     * Gets the goal selector of this entity. This can be used to add AI goals.
     *
     * @return the goal selector
     */
    public GoalSelector getGoalSelector() {
        return goalSelector;
    }

    /**
     * Adds a goal to the goal selector. The later the goal is added, the lower its priority.
     *
     * @param goal  the goal to add
     * @param slots the slots that the goal will occupy
     * @return this
     */
    public EntityAI addGoal(Goal goal, GoalSelector.Slot... slots) {
        goalSelector.getGoals().add(new GoalSelector.GoalInstance(goal, Set.of(slots)));
        return this;
    }

    /**
     * Returns a modifiable list of target selectors for this entity.
     * <p>
     * The order of this list determines priority (with the first selector being higher priority than the next, and so on).
     *
     * @return a modifiable list of target selectors
     */
    public List<TargetSelector> getTargetSelectors() {
        return targetSelectors;
    }

    /**
     * Adds a target selector. The later the target selector is added, the lower its priority.
     *
     * @param targetSelector the target selector to add
     * @return this
     */
    public EntityAI addTargetSelector(TargetSelector targetSelector) {
        targetSelectors.add(targetSelector);
        return this;
    }

    /**
     * Gets the entity target.
     *
     * @return the entity target, can be null if not any
     */
    @Nullable
    public Entity getTarget() {
        return target;
    }

    /**
     * Changes the entity target.
     *
     * @param target the new entity target, null to remove
     */
    public void setTarget(@Nullable Entity target) {
        this.target = target;
    }
}
