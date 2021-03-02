package net.minestom.server.entity.ai;

import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * Represents a group of entity's AI.
 * It may contains {@link GoalSelector goal selectors} and {@link TargetSelector target selectors}.
 * All AI groups of a single entity are independent of each other.
 */
public class EntityAIGroup {

    private GoalSelector currentGoalSelector;
    private final List<GoalSelector> goalSelectors = new GoalSelectorsArrayList();
    private final List<TargetSelector> targetSelectors = new ArrayList<>();

    /**
     * Gets the goal selectors of this group.
     *
     * @return a modifiable list containing this group goal selectors
     */
    @NotNull
    public List<GoalSelector> getGoalSelectors() {
        return this.goalSelectors;
    }

    /**
     * Gets the target selectors of this group.
     *
     * @return a modifiable list containing this group target selectors
     */
    @NotNull
    public List<TargetSelector> getTargetSelectors() {
        return this.targetSelectors;
    }

    /**
     * Gets the current goal selector of this group.
     *
     * @return the current goal selector of this group, null if not any
     */
    @Nullable
    public GoalSelector getCurrentGoalSelector() {
        return this.currentGoalSelector;
    }

    /**
     * Changes the current goal selector of this group.
     * <p>
     * Mostly unsafe since the current goal selector should normally
     * be chosen during the group tick method.
     *
     * @param goalSelector the new goal selector of this group, null to disable it
     */
    public void setCurrentGoalSelector(@Nullable GoalSelector goalSelector) {
        Check.argCondition(
                goalSelector != null && goalSelector.getAIGroup() != this,
                "Tried to set goal selector attached to another AI group!"
        );
        this.currentGoalSelector = goalSelector;
    }

    public void tick(long time) {
        GoalSelector currentGoalSelector = getCurrentGoalSelector();

        if (currentGoalSelector != null && currentGoalSelector.shouldEnd()) {
            currentGoalSelector.end();
            currentGoalSelector = null;
            setCurrentGoalSelector(null);
        }

        for (GoalSelector selector : getGoalSelectors()) {
            if (selector == currentGoalSelector) {
                break;
            }
            if (selector.shouldStart()) {
                if (currentGoalSelector != null) {
                    currentGoalSelector.end();
                }
                currentGoalSelector = selector;
                setCurrentGoalSelector(currentGoalSelector);
                currentGoalSelector.start();
                break;
            }
        }

        if (currentGoalSelector != null) {
            currentGoalSelector.tick(time);
        }
    }

    /**
     * The purpose of this list is to guarantee that every {@link GoalSelector} added to that group
     * has a reference to it for some internal interactions. We don't provide developers with
     * methods like `addGoalSelector` or `removeGoalSelector`: instead we provide them with direct
     * access to list of goal selectors, so that they could use operations such as `clear`, `set`, `removeIf`, etc.
     */
    private class GoalSelectorsArrayList extends ArrayList<GoalSelector> {

        private GoalSelectorsArrayList() {
        }

        @Override
        public GoalSelector set(int index, GoalSelector element) {
            element.setAIGroup(EntityAIGroup.this);
            return super.set(index, element);
        }

        @Override
        public boolean add(GoalSelector element) {
            element.setAIGroup(EntityAIGroup.this);
            return super.add(element);
        }

        @Override
        public void add(int index, GoalSelector element) {
            element.setAIGroup(EntityAIGroup.this);
            super.add(index, element);
        }

        @Override
        public boolean addAll(Collection<? extends GoalSelector> c) {
            c.forEach(goalSelector -> goalSelector.setAIGroup(EntityAIGroup.this));
            return super.addAll(c);
        }

        @Override
        public boolean addAll(int index, Collection<? extends GoalSelector> c) {
            c.forEach(goalSelector -> goalSelector.setAIGroup(EntityAIGroup.this));
            return super.addAll(index, c);
        }

        @Override
        public void replaceAll(UnaryOperator<GoalSelector> operator) {
            super.replaceAll(goalSelector -> {
                goalSelector = operator.apply(goalSelector);
                goalSelector.setAIGroup(EntityAIGroup.this);
                return goalSelector;
            });
        }

    }

}
