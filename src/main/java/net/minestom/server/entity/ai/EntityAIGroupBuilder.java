package net.minestom.server.entity.ai;

import java.util.Set;

public class EntityAIGroupBuilder {

    private final GoalSelector group = new GoalSelector();

    /**
     * Adds {@link Goal} to the list of goal selectors of the building {@link GoalSelector}.
     * Addition order is also a priority: priority the higher the earlier selector was added.
     *
     * @param goal goal selector to be added.
     * @return this builder.
     */
    public EntityAIGroupBuilder addGoalSelector(Goal goal, GoalSelector.Slot... slots) {
        this.group.getGoals().add(new GoalSelector.GoalInstance(goal, Set.of(slots)));
        return this;
    }

    /**
     * Adds {@link TargetSelector} to the list of target selectors of the building {@link GoalSelector}.
     * Addition order is also a priority: priority the higher the earlier selector was added.
     *
     * @param targetSelector target selector to be added.
     * @return this builder.
     */
//    public EntityAIGroupBuilder addTargetSelector(TargetSelector targetSelector) {
//        this.group.getTargetSelectors().add(targetSelector);
//        return this;
//    }

    /**
     * Creates new {@link GoalSelector}.
     *
     * @return new {@link GoalSelector}.
     */
    public GoalSelector build() {
        return this.group;
    }

}
