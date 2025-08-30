package net.minestom.server.entity.ai;

import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.UnaryOperator;

/**
 * Responsible for regulating entity AI goals.
 * <p>
 * The goals are organized by slots. A {@link Slot} can be user created.
 * Each slot can hold one active goal, but a goal can occupy multiple slots if needed.
 * <p>
 * Use {@link GoalSelector#getGoals()} to modify the goals.
 */
public class GoalSelector {
    private final Map<Slot, GoalInstance> currentGoals = new HashMap<>();
    private final List<GoalInstance> goals = new GoalArrayList();

    /**
     * Returns a modifiable list of {@link GoalInstance} objects.
     * <p>
     * The order of this list determines goal priority (with the first goal being higher priority than the next, and so on).
     *
     * @return a modifiable list of goals
     */
    public List<GoalInstance> getGoals() {
        return goals;
    }

    /**
     * Adds a goal to the goal list. The later the goal is added, the lower its priority.
     *
     * @param goal  the goal to add
     * @param slots the slots that the goal will occupy
     * @return this
     */
    public GoalSelector addGoal(Goal goal, Slot slot, Slot... slots) {
        Set<Slot> slotSet = new HashSet<>();
        slotSet.add(slot);
        Collections.addAll(slotSet, slots);

        goals.add(new GoalInstance(goal, slotSet));
        return this;
    }

    /**
     * Gets the current active goal for the given slot.
     *
     * @param slot the slot to get the active goal from
     * @return the current goal selector of this group, null if not any
     */
    public @Nullable GoalInstance getCurrentGoal(Slot slot) {
        return currentGoals.get(slot);
    }

    /**
     * Changes the current active goal for the given slot.
     * <p>
     * Mostly unsafe since the current goal should normally
     * be chosen during the tick method.
     * <p>
     * NOTE: This method will only update the given slot, not all slots defined by the {@link GoalInstance}.
     *
     * @param goalInstance the new goal for this slot, null to disable it
     */
    public void setCurrentGoal(Slot slot, @Nullable GoalInstance goalInstance) {
        Check.argCondition(
                goalInstance != null && goalInstance.goal.getAIGroup() != this,
                "Tried to set goal selector attached to another AI group!"
        );
        if (goalInstance != null) {
            currentGoals.put(slot, goalInstance);
        } else {
            currentGoals.remove(slot);
        }
    }

    public void tick(long time) {
        // End and remove goals when needed
        for (GoalInstance goalInstance : goals) {
            if (goalInstance.active && (goalInstance.disabled || goalInstance.goal.shouldEnd())) {
                goalInstance.goal.end();
                goalInstance.active = false;

                for (Slot slot : goalInstance.slots) {
                    currentGoals.remove(slot);
                }
            }
        }

        // Start new goals if needed
        for (GoalInstance goalInstance : goals) {
            if (goalInstance.disabled || goalInstance.active)
                continue;

            if (canReplaceAllSlots(goalInstance) && goalInstance.goal.canStart()) {
                for (Slot slot : goalInstance.slots) {
                    GoalInstance prevGoal = currentGoals.put(slot, goalInstance);
                    if (prevGoal != null && prevGoal.active) {
                        prevGoal.goal.end();
                        prevGoal.active = false;
                    }
                }

                goalInstance.goal.start();
                goalInstance.active = true;
            }
        }

        // Tick current goals
        Set<GoalInstance> tickedGoals = new HashSet<>();
        for (GoalInstance goalInstance : currentGoals.values()) {
            if (tickedGoals.contains(goalInstance)) continue; // Do not tick twice
            goalInstance.goal.tick(time);
            tickedGoals.add(goalInstance);
        }
    }

    /**
     * Returns whether all the needed slots for the given goal can be replaced.
     * It takes into account the priority (list order) in which the goals were defined.
     *
     * @param goal the goal for which to determine the result
     * @return whether this goal can replace all current goals in the needed slots
     */
    private boolean canReplaceAllSlots(GoalInstance goal) {
        int priority = goals.indexOf(goal);

        for (Slot slot : goal.slots) {
            GoalInstance current = currentGoals.get(slot);
            if (current == null) continue;
            int runningPriority = goals.indexOf(current);
            if (!current.goal.canInterrupt() || priority > runningPriority)
                return false;
        }
        return true;
    }

    /**
     * Represents a goal selector slot, which can have an active goal.
     */
    public static class Slot {}

    public static class GoalInstance {
        private final Goal goal;
        private final Set<Slot> slots;
        private boolean active;
        private boolean disabled;

        public GoalInstance(Goal goal, Set<Slot> slots) {
            this.goal = goal;
            this.slots = Set.copyOf(slots);
        }

        public Goal goal() {
            return goal;
        }

        public Set<Slot> slots() {
            return slots;
        }

        public boolean isActive() {
            return active;
        }

        public boolean isDisabled() {
            return disabled;
        }

        public void setDisabled(boolean disabled) {
            this.disabled = disabled;
        }
    }

    /**
     * The purpose of this list is to guarantee that every {@link Goal} added to that group
     * has a reference to it for some internal interactions. We don't provide developers with
     * methods like `addGoal` or `removeGoal`: instead we provide them with direct
     * access to list of goals, so that they could use operations such as `clear`, `set`, `removeIf`, etc.
     */
    private class GoalArrayList extends ArrayList<GoalInstance> {

        private GoalArrayList() {
        }

        @Override
        public GoalInstance set(int index, GoalInstance element) {
            element.goal().setAIGroup(GoalSelector.this);
            return super.set(index, element);
        }

        @Override
        public boolean add(GoalInstance element) {
            element.goal().setAIGroup(GoalSelector.this);
            return super.add(element);
        }

        @Override
        public void add(int index, GoalInstance element) {
            element.goal().setAIGroup(GoalSelector.this);
            super.add(index, element);
        }

        @Override
        public boolean addAll(Collection<? extends GoalInstance> c) {
            c.forEach(element -> element.goal().setAIGroup(GoalSelector.this));
            return super.addAll(c);
        }

        @Override
        public boolean addAll(int index, Collection<? extends GoalInstance> c) {
            c.forEach(element -> element.goal().setAIGroup(GoalSelector.this));
            return super.addAll(index, c);
        }

        @Override
        public void replaceAll(UnaryOperator<GoalInstance> operator) {
            super.replaceAll(element -> {
                element = operator.apply(element);
                element.goal().setAIGroup(GoalSelector.this);
                return element;
            });
        }

    }

}
