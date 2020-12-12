package net.minestom.server.entity.ai;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents an entity which can contain
 * {@link GoalSelector goal selectors} and {@link TargetSelector target selectors}.
 */
public interface EntityAI {

    /**
     * Gets the goal selectors of this entity.
     *
     * @return a modifiable list containing the entity goal selectors
     */
    @NotNull
    List<GoalSelector> getGoalSelectors();

    /**
     * Gets the target selectors of this entity.
     *
     * @return a modifiable list containing the entity target selectors
     */
    @NotNull
    List<TargetSelector> getTargetSelectors();

    /**
     * Gets the current entity goal selector.
     *
     * @return the current entity goal selector, null if not any
     */
    @Nullable
    GoalSelector getCurrentGoalSelector();

    /**
     * Changes the entity current goal selector.
     * <p>
     * Mostly unsafe since the current goal selector should normally
     * be chosen during the entity tick method.
     *
     * @param goalSelector the new entity goal selector, null to disable it
     */
    void setCurrentGoalSelector(@Nullable GoalSelector goalSelector);

    /**
     * Performs an AI tick, it includes finding a new {@link GoalSelector}
     * or tick the current one,
     *
     * @param time the tick time in milliseconds
     */
    default void aiTick(long time) {
        GoalSelector currentGoalSelector = getCurrentGoalSelector();
        // true if the goal selector changed this tick
        boolean newGoalSelector = false;

        if (currentGoalSelector == null) {
            // No goal selector, get a new one
            currentGoalSelector = findGoal();
            newGoalSelector = currentGoalSelector != null;
        } else {
            final boolean stop = currentGoalSelector.shouldEnd();
            if (stop) {
                // The current goal selector stopped, find a new one
                currentGoalSelector.end();
                currentGoalSelector = findGoal();
                newGoalSelector = currentGoalSelector != null;
            }
        }

        // Start the new goal selector
        if (newGoalSelector) {
            setCurrentGoalSelector(currentGoalSelector);
            currentGoalSelector.start();
        }

        // Execute tick for the current goal selector
        if (currentGoalSelector != null) {
            currentGoalSelector.tick(time);
        }
    }

    /**
     * Finds a new {@link GoalSelector} for the entity.
     * <p>
     * Uses {@link GoalSelector#shouldStart()} and return the goal selector if true.
     *
     * @return the goal selector found, null if not any
     */
    private GoalSelector findGoal() {
        for (GoalSelector goalSelector : getGoalSelectors()) {
            final boolean start = goalSelector.shouldStart();
            if (start) {
                return goalSelector;
            }
        }
        return null;
    }

}
