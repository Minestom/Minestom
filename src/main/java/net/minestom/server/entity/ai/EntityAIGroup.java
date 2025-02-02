package net.minestom.server.entity.ai;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * Represents a collection of {@link AIGoal ai goals}.
 */
public class EntityAIGroup {

    private AIGoal currentGoalSelector;
    private final List<AIGoal> goalSelectors = new ArrayList<>();

    /**
     * Gets the goal selectors of this group.
     *
     * @return an unmodifiable list containing this group's goal selectors
     */
    @NotNull
    public List<AIGoal> getGoalSelectors() {
        return Collections.unmodifiableList(goalSelectors);
    }

    public void addAIGoals(@NotNull List<AIGoal> goalSelectors) {
        this.goalSelectors.addAll(goalSelectors);
        Collections.sort(goalSelectors);
    }

    /**
     * Gets the currently active goal of this group
     *
     * @return the current AI goal of this group, null if not any
     */
    @Nullable
    public AIGoal getCurrentGoalSelector() {
        return this.currentGoalSelector;
    }

    public void tick(long time) {
        AIGoal currentGoalSelector = getCurrentGoalSelector();

         if (currentGoalSelector != null) {
            if (currentGoalSelector.shouldEnd()) {
                currentGoalSelector.end();
                this.currentGoalSelector = null;
            } else {
                currentGoalSelector.tick(time);
                // Check for any goal interrupts
                for (var goal : goalSelectors) {
                    if (goal.shouldInterrupt()) {
                        currentGoalSelector.end();
                        this.currentGoalSelector = goal;
                        goal.start();
                        break;
                    }
                }
            }
        }

        if (currentGoalSelector == null) {
            // We don't have a current goal, pick one with the highest priority
            for (var goal : goalSelectors) {
                if (goal.shouldStart()) {
                    this.currentGoalSelector = goal;
                    goal.start();
                    break;
                }
            }
        }
    }
}
