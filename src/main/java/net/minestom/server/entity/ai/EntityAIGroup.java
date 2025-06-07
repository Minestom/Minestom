package net.minestom.server.entity.ai;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a collection of {@link AIGoal ai goals}.
 */
public class EntityAIGroup {

    private AIGoal currentAIGoal;
    private final List<AIGoal> aiGoals = new ArrayList<>();

    /**
     * Gets the goal selectors of this group.
     *
     * @return an unmodifiable list containing this group's goal selectors
     */
    @NotNull
    public List<AIGoal> getAIGoals() {
        return Collections.unmodifiableList(aiGoals);
    }

    public void addAIGoals(@NotNull List<AIGoal> goalSelectors) {
        this.aiGoals.addAll(goalSelectors);
        Collections.sort(goalSelectors);
    }

    /**
     * Gets the currently active goal of this group
     *
     * @return the current AI goal of this group, null if not any
     */
    @Nullable
    public AIGoal getCurrentAIGoal() {
        return this.currentAIGoal;
    }

    public void tick(long time) {
        AIGoal currentGoal = getCurrentAIGoal();

         if (currentGoal != null) {
            if (currentGoal.shouldEnd()) {
                currentGoal.end();
                this.currentAIGoal = null;
            } else {
                currentGoal.tick(time);
                // Check for any goal interrupts
                for (var goal : aiGoals) {
                    if (goal.canInterrupt(currentGoal) && goal.shouldStart()) {
                        currentGoal.end();
                        this.currentAIGoal = goal;
                        goal.start();
                        break;
                    }
                }
            }
        }

        if (currentGoal == null) {
            // We don't have a current goal, pick one with the highest priority
            for (var goal : aiGoals) {
                if (goal.shouldStart()) {
                    this.currentAIGoal = goal;
                    goal.start();
                    break;
                }
            }
        }
    }
}
